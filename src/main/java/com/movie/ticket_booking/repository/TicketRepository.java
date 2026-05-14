package com.movie.ticket_booking.repository;

import com.movie.ticket_booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByShowtimeIdAndSeatId(Long showtimeId, Long seatId);

    List<Ticket> findByBookingIdOrderByIdAsc(Long bookingId);

    @Query("select t.seat.id from Ticket t where t.showtime.id = :showtimeId")
    List<Long> findSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);
}
