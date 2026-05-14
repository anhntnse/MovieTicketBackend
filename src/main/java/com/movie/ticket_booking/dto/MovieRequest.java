package com.movie.ticket_booking.dto;

import com.movie.ticket_booking.entity.enums.AgeRating;
import com.movie.ticket_booking.entity.enums.MovieFormat;
import com.movie.ticket_booking.entity.enums.MovieStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRequest {

    @NotBlank
    private String title;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must be lowercase and hyphen-separated")
    private String slug;

    @NotBlank
    private String posterUrl;

    @NotNull
    private AgeRating ageRating;

    @NotNull
    private MovieStatus status;

    @NotNull
    @Positive
    private Integer duration;

    private LocalDate releaseDate;

    private String trailerUrl;

    private String description;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal ratingScore;

    @Builder.Default
    private List<@NotBlank String> cast = List.of();

    @Builder.Default
    private Set<MovieFormat> formats = Set.of();

    @Builder.Default
    private List<@NotNull Long> genreIds = List.of();
}
