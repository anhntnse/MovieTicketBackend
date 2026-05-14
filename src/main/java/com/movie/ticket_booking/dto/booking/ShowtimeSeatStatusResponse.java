package com.movie.ticket_booking.dto.booking;

import com.movie.ticket_booking.entity.enums.SeatType;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeSeatStatusResponse {

    private Long seatId;
    private String label;
    private Integer rowNumber;
    private Integer columnNumber;
    private SeatType type;
    private boolean active;

    private ShowtimeSeatStatus status;

    private Instant holdExpiresAt;
}
