package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.dto.SeatUpdateRequest;
import com.movie.ticket_booking.entity.Seat;
import com.movie.ticket_booking.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    public SeatServiceImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public SeatResponse update(Long id, SeatUpdateRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        if (request.getType() != null) {
            seat.setType(request.getType());
        }
        if (request.getActive() != null) {
            seat.setActive(request.getActive());
        }

        Seat saved = seatRepository.saveAndFlush(seat);
        return SeatResponse.builder()
                .id(saved.getId())
                .label(saved.getLabel())
                .rowNumber(saved.getRowNumber())
                .columnNumber(saved.getColumnNumber())
                .type(saved.getType())
                .active(saved.isActive())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
