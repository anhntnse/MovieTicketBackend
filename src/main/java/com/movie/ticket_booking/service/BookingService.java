package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.booking.*;

import java.util.List;

public interface BookingService {

    List<ShowtimeSeatStatusResponse> getShowtimeSeats(Long showtimeId, String requesterEmail);

    List<HoldSeatResponse> holdSeats(String requesterEmail, HoldSeatsRequest request);

    void releaseSeats(String requesterEmail, ReleaseSeatsRequest request);

    BookingCheckoutResponse checkout(String requesterEmail, CheckoutRequest request);
}
