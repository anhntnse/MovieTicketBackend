package com.movie.ticket_booking.config;

import com.movie.ticket_booking.repository.SeatHoldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@EnableScheduling
@Slf4j
public class BookingHoldCleanupJob {

    private final SeatHoldRepository seatHoldRepository;

    public BookingHoldCleanupJob(SeatHoldRepository seatHoldRepository) {
        this.seatHoldRepository = seatHoldRepository;
    }

    @Scheduled(fixedDelayString = "${app.booking.cleanup-delay-ms:60000}")
    @Transactional
    public void cleanupExpiredHolds() {
        int deleted = seatHoldRepository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Cleanup expired seat holds: deleted={}", deleted);
        }
    }
}
