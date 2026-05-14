package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.GenreResponse;
import com.movie.ticket_booking.dto.MovieRequest;
import com.movie.ticket_booking.dto.MovieResponse;
import com.movie.ticket_booking.entity.Genre;
import com.movie.ticket_booking.entity.Movie;
import com.movie.ticket_booking.repository.GenreRepository;
import com.movie.ticket_booking.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    public MovieServiceImpl(MovieRepository movieRepository, GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getAll() {
        return movieRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        return toResponse(movie);
    }

    @Override
    public MovieResponse create(MovieRequest request) {
        if (movieRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Movie slug already exists");
        }

        Movie movie = Movie.builder().build();
        applyRequest(movie, request);
        Movie saved = movieRepository.saveAndFlush(movie);
        return toResponse(saved);
    }

    @Override
    public MovieResponse update(UUID id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));

        movieRepository.findBySlug(request.getSlug())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Movie slug already exists");
                });

        applyRequest(movie, request);
        Movie saved = movieRepository.saveAndFlush(movie);
        return toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
        }
        movieRepository.deleteById(id);
    }

    private void applyRequest(Movie movie, MovieRequest request) {
        movie.setTitle(request.getTitle());
        movie.setSlug(request.getSlug());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setAgeRating(request.getAgeRating());
        movie.setStatus(request.getStatus());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setDescription(request.getDescription());
        movie.setRatingScore(request.getRatingScore());

        movie.setCast(new ArrayList<>(Optional.ofNullable(request.getCast()).orElseGet(List::of)));
        movie.setFormats(new HashSet<>(Optional.ofNullable(request.getFormats()).orElseGet(Set::of)));

        List<Long> genreIds = Optional.ofNullable(request.getGenreIds()).orElseGet(List::of);
        if (genreIds.isEmpty()) {
            movie.setGenres(new HashSet<>());
        } else {
            List<Genre> genres = genreRepository.findAllById(genreIds);
            Set<Long> found = genres.stream().map(Genre::getId).collect(Collectors.toSet());
            List<Long> missing = genreIds.stream().filter(id -> !found.contains(id)).toList();
            if (!missing.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown genreIds: " + missing);
            }
            movie.setGenres(new HashSet<>(genres));
        }
    }

    private MovieResponse toResponse(Movie movie) {
        List<GenreResponse> genreResponses = movie.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(g -> GenreResponse.builder().id(g.getId()).name(g.getName()).slug(g.getSlug()).build())
                .toList();

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .posterUrl(movie.getPosterUrl())
                .ageRating(movie.getAgeRating())
                .status(movie.getStatus())
                .duration(movie.getDuration())
                .releaseDate(movie.getReleaseDate())
                .trailerUrl(movie.getTrailerUrl())
                .description(movie.getDescription())
                .ratingScore(movie.getRatingScore())
                .cast(new ArrayList<>(movie.getCast()))
                .formats(new HashSet<>(movie.getFormats()))
                .genres(genreResponses)
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
