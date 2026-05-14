package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.GenreRequest;
import com.movie.ticket_booking.dto.GenreResponse;
import com.movie.ticket_booking.entity.Genre;
import com.movie.ticket_booking.repository.GenreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAll() {
        return genreRepository.findAll().stream()
                .sorted(Comparator.comparing(Genre::getName, String::compareToIgnoreCase))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GenreResponse create(GenreRequest request) {
        genreRepository.findBySlug(request.getSlug())
                .ifPresent(g -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Genre slug already exists");
                });

        Genre genre = Genre.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();

        Genre saved = genreRepository.saveAndFlush(genre);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found");
        }
        genreRepository.deleteById(id);
    }

    private GenreResponse toResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .slug(genre.getSlug())
                .build();
    }
}
