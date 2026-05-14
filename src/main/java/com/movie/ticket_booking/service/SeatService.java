package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.dto.SeatUpdateRequest;

public interface SeatService {

    SeatResponse update(Long id, SeatUpdateRequest request);
}
