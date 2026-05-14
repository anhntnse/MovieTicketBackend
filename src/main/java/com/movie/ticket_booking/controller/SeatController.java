package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.dto.SeatUpdateRequest;
import com.movie.ticket_booking.service.SeatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@Slf4j
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PatchMapping("/{id}")
    public SeatResponse updateSeat(@PathVariable Long id, @Valid @RequestBody SeatUpdateRequest request) {
        log.info("PATCH /api/seats/{} type={} active={}", id, request.getType(), request.getActive());
        SeatResponse updated = seatService.update(id, request);
        log.info("PATCH /api/seats/{} -> 200 type={} active={}", id, updated.getType(), updated.isActive());
        return updated;
    }
}
