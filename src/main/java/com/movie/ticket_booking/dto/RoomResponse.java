package com.movie.ticket_booking.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;

    private String code;

    private String name;

    private Integer totalRows;

    private Integer totalColumns;

    private Instant createdAt;

    private Instant updatedAt;
}
