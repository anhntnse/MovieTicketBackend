package com.movie.ticket_booking.repository;

import com.movie.ticket_booking.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sh from SeatHold sh join fetch sh.user u where sh.showtime.id = :showtimeId and sh.seat.id = :seatId")
    Optional<SeatHold> findByShowtimeIdAndSeatIdForUpdate(@Param("showtimeId") Long showtimeId, @Param("seatId") Long seatId);

    @Query("select sh from SeatHold sh join fetch sh.seat s " +
            "where sh.showtime.id = :showtimeId " +
            "and sh.releasedAt is null and sh.consumedAt is null and sh.expiresAt > :now")
    List<SeatHold> findActiveByShowtimeId(@Param("showtimeId") Long showtimeId, @Param("now") Instant now);

    @Modifying
    @Query("delete from SeatHold sh where sh.expiresAt <= :now and sh.consumedAt is null")
    int deleteExpired(@Param("now") Instant now);
}
