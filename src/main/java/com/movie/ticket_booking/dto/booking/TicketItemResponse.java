package com.movie.ticket_booking.dto.booking;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketItemResponse {

    private Long ticketId;
    private Long seatId;
    private String seatLabel;
    private BigDecimal price;
    private String code;
}
