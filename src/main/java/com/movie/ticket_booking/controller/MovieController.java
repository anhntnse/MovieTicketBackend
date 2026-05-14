package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.MovieRequest;
import com.movie.ticket_booking.dto.MovieResponse;
import com.movie.ticket_booking.service.MovieService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@Slf4j
public class MovieController {
    
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<MovieResponse> getAllMovies() {
        log.info("GET /api/movies");
        List<MovieResponse> result = movieService.getAll();
        log.info("GET /api/movies -> {} item(s)", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable UUID id) {
        log.info("GET /api/movies/{}", id);
        MovieResponse result = movieService.getById(id);
        log.info("GET /api/movies/{} -> slug={}", id, result.getSlug());
        return result;
    }

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        log.info(
                "POST /api/movies slug={} title={} genreIds={} formats={} cast={}",
                request.getSlug(),
                request.getTitle(),
                request.getGenreIds() == null ? 0 : request.getGenreIds().size(),
                request.getFormats() == null ? 0 : request.getFormats().size(),
                request.getCast() == null ? 0 : request.getCast().size()
        );
        MovieResponse created = movieService.create(request);
        log.info("POST /api/movies -> 201 id={} slug={}", created.getId(), created.getSlug());
        return ResponseEntity.created(URI.create("/api/movies/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public MovieResponse updateMovie(@PathVariable UUID id, @Valid @RequestBody MovieRequest request) {
        log.info(
                "PUT /api/movies/{} slug={} title={} genreIds={} formats={} cast={}",
                id,
                request.getSlug(),
                request.getTitle(),
                request.getGenreIds() == null ? 0 : request.getGenreIds().size(),
                request.getFormats() == null ? 0 : request.getFormats().size(),
                request.getCast() == null ? 0 : request.getCast().size()
        );
        MovieResponse updated = movieService.update(id, request);
        log.info("PUT /api/movies/{} -> 200 slug={}", id, updated.getSlug());
        return updated;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
        log.info("DELETE /api/movies/{}", id);
        movieService.delete(id);
        log.info("DELETE /api/movies/{} -> 204", id);
        return ResponseEntity.noContent().build();
    }
}