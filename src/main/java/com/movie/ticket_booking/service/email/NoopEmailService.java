package com.movie.ticket_booking.service.email;

import com.movie.ticket_booking.entity.Booking;
import com.movie.ticket_booking.entity.Ticket;
import com.movie.ticket_booking.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class NoopEmailService implements EmailService {

    @Override
    public void sendBookingConfirmation(Booking booking, List<Ticket> tickets) {
        log.info("Email disabled: skip booking confirmation bookingId={} to={}", booking.getId(), booking.getContactEmail());
    }

    @Override
    public void sendWelcomeEmail(User user) {
        log.info("Email disabled: skip welcome email userId={} to={}", user.getId(), user.getEmail());
    }

    @Override
    public void sendLoginNotification(User user, Instant at, String ipAddress, String userAgent) {
        log.info("Email disabled: skip login notification userId={} to={} at={}", user.getId(), user.getEmail(), at);
    }
}
