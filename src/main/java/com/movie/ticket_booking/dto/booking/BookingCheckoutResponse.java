package com.movie.ticket_booking.dto.booking;

import com.movie.ticket_booking.entity.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCheckoutResponse {

    private Long bookingId;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private Instant paidAt;

    private List<TicketItemResponse> tickets;
}
