package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.MovieSummaryResponse;
import com.movie.ticket_booking.dto.RoomSummaryResponse;
import com.movie.ticket_booking.dto.ShowtimeRequest;
import com.movie.ticket_booking.dto.ShowtimeResponse;
import com.movie.ticket_booking.entity.Movie;
import com.movie.ticket_booking.entity.Room;
import com.movie.ticket_booking.entity.Showtime;
import com.movie.ticket_booking.repository.MovieRepository;
import com.movie.ticket_booking.repository.RoomRepository;
import com.movie.ticket_booking.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository, MovieRepository movieRepository, RoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getAll(UUID movieId, Long roomId, Instant from, Instant to) {
        return showtimeRepository.findAll().stream()
                .filter(s -> movieId == null || (s.getMovie() != null && movieId.equals(s.getMovie().getId())))
                .filter(s -> roomId == null || (s.getRoom() != null && roomId.equals(s.getRoom().getId())))
                .filter(s -> from == null || !s.getStartTime().isBefore(from))
                .filter(s -> to == null || !s.getStartTime().isAfter(to))
                .sorted(Comparator.comparing(Showtime::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShowtimeResponse getById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));
        return toResponse(showtime);
    }

    @Override
    public ShowtimeResponse create(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (showtimeRepository.existsOverlappingInRoom(room.getId(), request.getStartTime(), request.getEndTime(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Showtime overlaps with an existing showtime in this room");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .basePrice(request.getBasePrice())
                .build();

        Showtime saved = showtimeRepository.saveAndFlush(showtime);
        return toResponse(saved);
    }

    @Override
    public ShowtimeResponse update(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        if (showtimeRepository.existsOverlappingInRoom(room.getId(), request.getStartTime(), request.getEndTime(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Showtime overlaps with an existing showtime in this room");
        }

        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        showtime.setBasePrice(request.getBasePrice());

        Showtime saved = showtimeRepository.saveAndFlush(showtime);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found");
        }
        showtimeRepository.deleteById(id);
    }

    private void validateTimeRange(Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }
    }

    private ShowtimeResponse toResponse(Showtime showtime) {
        Movie movie = showtime.getMovie();
        Room room = showtime.getRoom();

        MovieSummaryResponse movieSummary = movie == null ? null : MovieSummaryResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .posterUrl(movie.getPosterUrl())
                .build();

        RoomSummaryResponse roomSummary = room == null ? null : RoomSummaryResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .build();

        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movie(movieSummary)
                .room(roomSummary)
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .createdAt(showtime.getCreatedAt())
                .updatedAt(showtime.getUpdatedAt())
                .build();
    }
}
