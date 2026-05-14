package com.movie.ticket_booking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeResponse {

    private Long id;

    private MovieSummaryResponse movie;

    private RoomSummaryResponse room;

    private Instant startTime;

    private Instant endTime;

    private BigDecimal basePrice;

    private Instant createdAt;

    private Instant updatedAt;
}
