package com.movie.ticket_booking.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSummaryResponse {

    private UUID id;

    private String title;

    private String slug;

    private String posterUrl;
}
