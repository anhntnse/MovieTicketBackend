package com.movie.ticket_booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tickets_showtime_seat", columnNames = {"showtime_id", "seat_id"})
        },
        indexes = {
                @Index(name = "idx_tickets_booking", columnList = "booking_id"),
                @Index(name = "idx_tickets_showtime", columnList = "showtime_id"),
                @Index(name = "idx_tickets_seat", columnList = "seat_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
        @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
        @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
        @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seat seat;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 64)
    private String code; // mã vé gửi email

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
