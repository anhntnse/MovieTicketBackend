package com.movie.ticket_booking.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseSeatsRequest {

    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<@NotNull Long> seatIds;
}
