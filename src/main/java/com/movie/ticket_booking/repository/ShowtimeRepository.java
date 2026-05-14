package com.movie.ticket_booking.repository;

import com.movie.ticket_booking.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByRoomId(Long roomId);

        Optional<Showtime> findByRoomIdAndStartTime(Long roomId, Instant startTime);

    @Query("""
            select (count(s) > 0) from Showtime s
            where s.room.id = :roomId
              and s.startTime < :endTime
              and s.endTime > :startTime
              and (:excludeId is null or s.id <> :excludeId)
            """)
    boolean existsOverlappingInRoom(
            @Param("roomId") Long roomId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") Long excludeId
    );
}
