package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.booking.*;
import com.movie.ticket_booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/holds")
    public List<HoldSeatResponse> holdSeats(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody HoldSeatsRequest request
    ) {
        String email = jwt == null ? null : jwt.getSubject();
        log.info("POST /api/bookings/holds showtimeId={} seatCount={} user={}", request.getShowtimeId(), request.getSeatIds().size(), email);
        return bookingService.holdSeats(email, request);
    }

    @DeleteMapping("/holds")
    public ResponseEntity<Void> releaseSeats(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReleaseSeatsRequest request
    ) {
        String email = jwt == null ? null : jwt.getSubject();
        log.info("DELETE /api/bookings/holds showtimeId={} seatCount={} user={}", request.getShowtimeId(), request.getSeatIds().size(), email);
        bookingService.releaseSeats(email, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public BookingCheckoutResponse checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request
    ) {
        String email = jwt == null ? null : jwt.getSubject();
        log.info("POST /api/bookings/checkout showtimeId={} seatCount={} user={} provider={}",
                request.getShowtimeId(), request.getSeatIds().size(), email, request.getPaymentProvider());
        return bookingService.checkout(email, request);
    }
}
