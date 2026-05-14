package com.movie.ticket_booking.dto;

import com.movie.ticket_booking.entity.enums.SeatType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatUpdateRequest {

    private SeatType type;

    private Boolean active;
}
