package com.movie.ticket_booking.event;

import java.time.Instant;

public record UserLoggedInEvent(Long userId, Instant at, String ipAddress, String userAgent) {
}
