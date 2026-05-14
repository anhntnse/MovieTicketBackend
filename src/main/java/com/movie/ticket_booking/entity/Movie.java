package com.movie.ticket_booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.ticket_booking.entity.enums.AgeRating;
import com.movie.ticket_booking.entity.enums.MovieFormat;
import com.movie.ticket_booking.entity.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity 
@Table(
    name = "movies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_movies_slug", columnNames = "slug")
    },
    indexes = {
        @Index(name = "idx_movies_slug", columnList = "slug"),
        @Index(name = "idx_movies_status", columnList = "status"),
        @Index(name = "idx_movies_release_date", columnList = "release_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(nullable = false, length = 1000)
    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AgeRating ageRating;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_movie_genres_movie_genre", columnNames = {"movie_id", "genre_id"})
    )
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovieStatus status;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_formats", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "format", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    @Builder.Default
    private Set<MovieFormat> formats = new HashSet<>();

    @Column(nullable = false)
    private Integer duration; // Phút

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 1000)
    private String trailerUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 2, scale = 1)
    private BigDecimal ratingScore; // 0.0 - 5.0

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "cast_order")
    @Column(name = "cast_name", nullable = false, length = 200)
    @JsonIgnore
    @Builder.Default
    private List<String> cast = new ArrayList<>();

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Showtime> showtimes = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}