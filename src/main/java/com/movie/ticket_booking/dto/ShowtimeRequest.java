package com.movie.ticket_booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeRequest {

    @NotNull
    private UUID movieId;

    @NotNull
    private Long roomId;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal basePrice;
}
