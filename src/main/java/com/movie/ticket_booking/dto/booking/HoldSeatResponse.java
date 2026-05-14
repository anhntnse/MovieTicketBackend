package com.movie.ticket_booking.dto.booking;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatResponse {

    private Long seatId;
    private Long holdId;
    private Instant expiresAt;
}
