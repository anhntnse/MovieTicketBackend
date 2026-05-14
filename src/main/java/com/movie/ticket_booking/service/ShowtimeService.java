package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.ShowtimeRequest;
import com.movie.ticket_booking.dto.ShowtimeResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShowtimeService {

    List<ShowtimeResponse> getAll(UUID movieId, Long roomId, Instant from, Instant to);

    ShowtimeResponse getById(Long id);

    ShowtimeResponse create(ShowtimeRequest request);

    ShowtimeResponse update(Long id, ShowtimeRequest request);

    void delete(Long id);
}
