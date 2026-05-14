package com.movie.ticket_booking.dto;

import com.movie.ticket_booking.entity.enums.AgeRating;
import com.movie.ticket_booking.entity.enums.MovieFormat;
import com.movie.ticket_booking.entity.enums.MovieStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {

    private UUID id;

    private String title;

    private String slug;

    private String posterUrl;

    private AgeRating ageRating;

    private MovieStatus status;

    private Integer duration;

    private LocalDate releaseDate;

    private String trailerUrl;

    private String description;

    private BigDecimal ratingScore;

    private List<String> cast;

    private Set<MovieFormat> formats;

    private List<GenreResponse> genres;

    private Instant createdAt;

    private Instant updatedAt;
}
