package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.RoomRequest;
import com.movie.ticket_booking.dto.RoomResponse;
import com.movie.ticket_booking.dto.SeatResponse;

import java.util.List;

public interface RoomService {

    List<RoomResponse> getAll();

    RoomResponse getById(Long id);

    RoomResponse create(RoomRequest request);

    RoomResponse update(Long id, RoomRequest request);

    void delete(Long id);

    List<SeatResponse> getSeats(Long roomId);
}
