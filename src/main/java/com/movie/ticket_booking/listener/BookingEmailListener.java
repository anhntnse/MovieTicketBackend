package com.movie.ticket_booking.listener;

import com.movie.ticket_booking.entity.Booking;
import com.movie.ticket_booking.repository.BookingRepository;
import com.movie.ticket_booking.repository.TicketRepository;
import com.movie.ticket_booking.event.BookingCheckedOutEvent;
import com.movie.ticket_booking.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class BookingEmailListener {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onBookingCheckedOut(BookingCheckedOutEvent event) {
        Booking booking = bookingRepository.findById(event.bookingId()).orElse(null);
        if (booking == null) {
            return;
        }
        emailService.sendBookingConfirmation(booking, ticketRepository.findByBookingIdOrderByIdAsc(booking.getId()));
    }
}
