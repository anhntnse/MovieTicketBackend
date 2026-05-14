package com.movie.ticket_booking.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreResponse {
    private Long id;
    private String name;
    private String slug;
}
