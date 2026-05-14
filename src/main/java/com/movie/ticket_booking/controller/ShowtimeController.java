package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.ShowtimeRequest;
import com.movie.ticket_booking.dto.ShowtimeResponse;
import com.movie.ticket_booking.dto.booking.ShowtimeSeatStatusResponse;
import com.movie.ticket_booking.service.BookingService;
import com.movie.ticket_booking.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/showtimes")
@Slf4j
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final BookingService bookingService;

    public ShowtimeController(ShowtimeService showtimeService, BookingService bookingService) {
        this.showtimeService = showtimeService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<ShowtimeResponse> getShowtimes(
            @RequestParam(required = false) UUID movieId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        log.info("GET /api/showtimes movieId={} roomId={} from={} to={}", movieId, roomId, from, to);
        List<ShowtimeResponse> result = showtimeService.getAll(movieId, roomId, from, to);
        log.info("GET /api/showtimes -> {} item(s)", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ShowtimeResponse getShowtimeById(@PathVariable Long id) {
        log.info("GET /api/showtimes/{}", id);
        ShowtimeResponse result = showtimeService.getById(id);
        log.info("GET /api/showtimes/{} -> movieId={} roomId={}", id,
                result.getMovie() == null ? null : result.getMovie().getId(),
                result.getRoom() == null ? null : result.getRoom().getId());
        return result;
    }

    @GetMapping("/{id}/seats")
    public java.util.List<ShowtimeSeatStatusResponse> getShowtimeSeats(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt == null ? null : jwt.getSubject();
        log.info("GET /api/showtimes/{}/seats requester={}", id, email == null ? "anonymous" : email);
        java.util.List<ShowtimeSeatStatusResponse> result = bookingService.getShowtimeSeats(id, email);
        log.info("GET /api/showtimes/{}/seats -> {} seat(s)", id, result.size());
        return result;
    }

    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        log.info("POST /api/showtimes movieId={} roomId={} start={} end={}", request.getMovieId(), request.getRoomId(), request.getStartTime(), request.getEndTime());
        ShowtimeResponse created = showtimeService.create(request);
        log.info("POST /api/showtimes -> 201 id={}", created.getId());
        return ResponseEntity.created(URI.create("/api/showtimes/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ShowtimeResponse updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
        log.info("PUT /api/showtimes/{} movieId={} roomId={} start={} end={}", id, request.getMovieId(), request.getRoomId(), request.getStartTime(), request.getEndTime());
        ShowtimeResponse updated = showtimeService.update(id, request);
        log.info("PUT /api/showtimes/{} -> 200", id);
        return updated;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        log.info("DELETE /api/showtimes/{}", id);
        showtimeService.delete(id);
        log.info("DELETE /api/showtimes/{} -> 204", id);
        return ResponseEntity.noContent().build();
    }
}
