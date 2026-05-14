package com.movie.ticket_booking.dto;

import com.movie.ticket_booking.entity.enums.SeatType;
import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {

    private Long id;

    private String label;

    private Integer rowNumber;

    private Integer columnNumber;

    private SeatType type;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}
