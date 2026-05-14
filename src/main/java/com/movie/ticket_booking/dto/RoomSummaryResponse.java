package com.movie.ticket_booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSummaryResponse {

    private Long id;

    private String code;

    private String name;
}
