package com.movie.ticket_booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.ticket_booking.entity.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seats_room_label", columnNames = {"room_id", "label"})
        },
        indexes = {
                @Index(name = "idx_seats_room", columnList = "room_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    @Column(nullable = false, length = 10)
    private String label; // Ví dụ: A1, A2...

    @Column(name = "seat_row", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_col", nullable = false)
    private Integer columnNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatType type = SeatType.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
