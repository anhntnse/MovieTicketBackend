package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.GenreRequest;
import com.movie.ticket_booking.dto.GenreResponse;
import com.movie.ticket_booking.service.GenreService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
@Slf4j
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> getAllGenres() {
        log.info("GET /api/genres");
        List<GenreResponse> result = genreService.getAll();
        log.info("GET /api/genres -> {} item(s)", result.size());
        return result;
    }

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(@Valid @RequestBody GenreRequest request) {
        log.info("POST /api/genres slug={} name={}", request.getSlug(), request.getName());
        GenreResponse created = genreService.create(request);
        log.info("POST /api/genres -> 201 id={} slug={}", created.getId(), created.getSlug());
        return ResponseEntity.created(URI.create("/api/genres/" + created.getId())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        log.info("DELETE /api/genres/{}", id);
        genreService.delete(id);
        log.info("DELETE /api/genres/{} -> 204", id);
        return ResponseEntity.noContent().build();
    }
}
