package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.GenreRequest;
import com.movie.ticket_booking.dto.GenreResponse;

import java.util.List;

public interface GenreService {
    List<GenreResponse> getAll();

    GenreResponse create(GenreRequest request);

    void delete(Long id);
}
