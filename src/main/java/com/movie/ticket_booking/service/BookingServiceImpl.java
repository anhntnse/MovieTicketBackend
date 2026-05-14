package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.booking.*;
import com.movie.ticket_booking.event.BookingCheckedOutEvent;
import com.movie.ticket_booking.entity.*;
import com.movie.ticket_booking.entity.enums.BookingStatus;
import com.movie.ticket_booking.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final long holdTtlSeconds;

    public BookingServiceImpl(
            UserRepository userRepository,
            ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            SeatHoldRepository seatHoldRepository,
            TicketRepository ticketRepository,
            BookingRepository bookingRepository,
                ApplicationEventPublisher applicationEventPublisher,
            @Value("${app.booking.hold-ttl-seconds:300}") long holdTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.holdTtlSeconds = holdTtlSeconds;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeSeatStatusResponse> getShowtimeSeats(Long showtimeId, String requesterEmail) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Long roomId = showtime.getRoom().getId();
        List<Seat> seats = seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(roomId);

        Instant now = Instant.now();
        Set<Long> soldSeatIds = new HashSet<>(ticketRepository.findSeatIdsByShowtimeId(showtimeId));

        Map<Long, SeatHold> activeHoldsBySeatId = new HashMap<>();
        for (SeatHold hold : seatHoldRepository.findActiveByShowtimeId(showtimeId, now)) {
            activeHoldsBySeatId.put(hold.getSeat().getId(), hold);
        }

        String normalizedEmail = requesterEmail == null ? null : requesterEmail.trim().toLowerCase();

        return seats.stream()
                .map(seat -> {
                    if (!seat.isActive()) {
                        return toStatusResponse(seat, ShowtimeSeatStatus.INACTIVE, null);
                    }
                    if (soldSeatIds.contains(seat.getId())) {
                        return toStatusResponse(seat, ShowtimeSeatStatus.SOLD, null);
                    }

                    SeatHold hold = activeHoldsBySeatId.get(seat.getId());
                    if (hold != null) {
                        String holdEmail = hold.getUser() == null ? null : hold.getUser().getEmail();
                        boolean mine = normalizedEmail != null && holdEmail != null && holdEmail.equalsIgnoreCase(normalizedEmail);
                        return toStatusResponse(seat, mine ? ShowtimeSeatStatus.HELD_BY_ME : ShowtimeSeatStatus.HELD, hold.getExpiresAt());
                    }

                    return toStatusResponse(seat, ShowtimeSeatStatus.AVAILABLE, null);
                })
                .toList();
    }

    @Override
    public List<HoldSeatResponse> holdSeats(String requesterEmail, HoldSeatsRequest request) {
        User user = findUserByEmailOrThrow(requesterEmail);
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Instant now = Instant.now();
        if (!now.isBefore(showtime.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot hold seats for a started/ended showtime");
        }

        Set<Long> seatIds = new LinkedHashSet<>(request.getSeatIds());
        if (seatIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds must not be empty");
        }

        Map<Long, Seat> seatsById = new HashMap<>();
        for (Seat seat : seatRepository.findAllById(seatIds)) {
            seatsById.put(seat.getId(), seat);
        }
        if (seatsById.size() != seatIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more seats not found");
        }

        Long showtimeRoomId = showtime.getRoom().getId();
        for (Seat seat : seatsById.values()) {
            if (!seat.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is inactive: " + seat.getLabel());
            }
            if (!Objects.equals(seat.getRoom().getId(), showtimeRoomId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat does not belong to the showtime room: " + seat.getId());
            }
            if (ticketRepository.existsByShowtimeIdAndSeatId(showtime.getId(), seat.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already sold: " + seat.getLabel());
            }
        }

        Instant expiresAt = now.plusSeconds(holdTtlSeconds);

        List<HoldSeatResponse> results = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatsById.get(seatId);
            SeatHold hold = upsertHoldWithLock(user, showtime, seat, now, expiresAt);
            results.add(HoldSeatResponse.builder()
                    .seatId(seatId)
                    .holdId(hold.getId())
                    .expiresAt(hold.getExpiresAt())
                    .build());
        }

        return results;
    }

    @Override
    public void releaseSeats(String requesterEmail, ReleaseSeatsRequest request) {
        User user = findUserByEmailOrThrow(requesterEmail);

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Instant now = Instant.now();
        Set<Long> seatIds = new LinkedHashSet<>(request.getSeatIds());

        for (Long seatId : seatIds) {
            seatHoldRepository.findByShowtimeIdAndSeatIdForUpdate(showtime.getId(), seatId)
                    .ifPresent(hold -> {
                        if (hold.getConsumedAt() != null) {
                            return;
                        }
                        if (hold.getReleasedAt() != null) {
                            return;
                        }
                        if (hold.getExpiresAt().isBefore(now)) {
                            return;
                        }
                        if (!Objects.equals(hold.getUser().getId(), user.getId())) {
                            return;
                        }

                        hold.setReleasedAt(now);
                        hold.setExpiresAt(now);
                        seatHoldRepository.save(hold);
                    });
        }
    }

    @Override
    public BookingCheckoutResponse checkout(String requesterEmail, CheckoutRequest request) {
        User user = findUserByEmailOrThrow(requesterEmail);
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Instant now = Instant.now();
        if (!now.isBefore(showtime.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout for a started/ended showtime");
        }

        Set<Long> seatIds = new LinkedHashSet<>(request.getSeatIds());
        if (seatIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "seatIds must not be empty");
        }

        Map<Long, Seat> seatsById = new HashMap<>();
        for (Seat seat : seatRepository.findAllById(seatIds)) {
            seatsById.put(seat.getId(), seat);
        }
        if (seatsById.size() != seatIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more seats not found");
        }

        Long showtimeRoomId = showtime.getRoom().getId();
        for (Seat seat : seatsById.values()) {
            if (!Objects.equals(seat.getRoom().getId(), showtimeRoomId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat does not belong to the showtime room: " + seat.getId());
            }
        }

        // Validate + lock holds
        List<SeatHold> lockedHolds = new ArrayList<>();
        for (Long seatId : seatIds) {
            SeatHold hold = seatHoldRepository.findByShowtimeIdAndSeatIdForUpdate(showtime.getId(), seatId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Seat is not held: " + seatId));

            if (hold.getConsumedAt() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already consumed");
            }
            if (hold.getReleasedAt() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat hold released");
            }
            if (hold.getExpiresAt().isBefore(now)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat hold expired");
            }
            if (!Objects.equals(hold.getUser().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat held by another user");
            }

            if (ticketRepository.existsByShowtimeIdAndSeatId(showtime.getId(), seatId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already sold");
            }

            lockedHolds.add(hold);
        }

        BigDecimal total = showtime.getBasePrice().multiply(BigDecimal.valueOf(seatIds.size()));

        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .status(BookingStatus.SUCCESS)
                .totalAmount(total)
                .paymentProvider(request.getPaymentProvider())
                .paymentTransactionId(generatePaymentTransactionId())
                .paidAt(now)
                .contactEmail(request.getContactEmail())
                .build();

        Booking savedBooking = bookingRepository.saveAndFlush(booking);

        List<TicketItemResponse> ticketItems = new ArrayList<>();
        try {
            for (Long seatId : seatIds) {
                Seat seat = seatsById.get(seatId);
                Ticket ticket = Ticket.builder()
                        .booking(savedBooking)
                        .showtime(showtime)
                        .seat(seat)
                        .price(showtime.getBasePrice())
                        .code(generateTicketCode())
                        .build();

                Ticket savedTicket = ticketRepository.save(ticket);
                ticketItems.add(TicketItemResponse.builder()
                        .ticketId(savedTicket.getId())
                        .seatId(seatId)
                        .seatLabel(seat.getLabel())
                        .price(savedTicket.getPrice())
                        .code(savedTicket.getCode())
                        .build());
            }
            ticketRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            log.warn("Checkout conflict: showtimeId={} seatIds={} userId={}", showtime.getId(), seatIds, user.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more seats were already booked", ex);
        }

        applicationEventPublisher.publishEvent(new BookingCheckedOutEvent(savedBooking.getId()));

        for (SeatHold hold : lockedHolds) {
            hold.setConsumedAt(now);
            hold.setReleasedAt(now);
            hold.setExpiresAt(now);
            seatHoldRepository.save(hold);
        }

        return BookingCheckoutResponse.builder()
                .bookingId(savedBooking.getId())
                .status(savedBooking.getStatus())
                .totalAmount(savedBooking.getTotalAmount())
                .paidAt(savedBooking.getPaidAt())
                .tickets(ticketItems)
                .build();
    }

    private ShowtimeSeatStatusResponse toStatusResponse(Seat seat, ShowtimeSeatStatus status, Instant holdExpiresAt) {
        return ShowtimeSeatStatusResponse.builder()
                .seatId(seat.getId())
                .label(seat.getLabel())
                .rowNumber(seat.getRowNumber())
                .columnNumber(seat.getColumnNumber())
                .type(seat.getType())
                .active(seat.isActive())
                .status(status)
                .holdExpiresAt(holdExpiresAt)
                .build();
    }

    private SeatHold upsertHoldWithLock(User user, Showtime showtime, Seat seat, Instant now, Instant expiresAt) {
        try {
            Optional<SeatHold> existingOpt = seatHoldRepository.findByShowtimeIdAndSeatIdForUpdate(showtime.getId(), seat.getId());
            if (existingOpt.isEmpty()) {
                SeatHold created = SeatHold.builder()
                        .user(user)
                        .showtime(showtime)
                        .seat(seat)
                        .expiresAt(expiresAt)
                        .releasedAt(null)
                        .consumedAt(null)
                        .build();
                return seatHoldRepository.saveAndFlush(created);
            }

            return takeOrExtendHold(user, existingOpt.get(), now, expiresAt);
        } catch (DataIntegrityViolationException ex) {
            // Another transaction inserted concurrently; retry by locking the row.
            SeatHold existing = seatHoldRepository.findByShowtimeIdAndSeatIdForUpdate(showtime.getId(), seat.getId())
                    .orElseThrow(() -> ex);
            return takeOrExtendHold(user, existing, now, expiresAt);
        }
    }

    private SeatHold takeOrExtendHold(User user, SeatHold hold, Instant now, Instant expiresAt) {
        if (hold.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already booked");
        }

        boolean expired = hold.getExpiresAt() == null || !hold.getExpiresAt().isAfter(now);
        boolean released = hold.getReleasedAt() != null;
        boolean active = !expired && !released;

        if (active) {
            if (!Objects.equals(hold.getUser().getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is locked by another user");
            }
            hold.setExpiresAt(expiresAt);
            return seatHoldRepository.saveAndFlush(hold);
        }

        hold.setUser(user);
        hold.setReleasedAt(null);
        hold.setExpiresAt(expiresAt);
        return seatHoldRepository.saveAndFlush(hold);
    }

    private User findUserByEmailOrThrow(String requesterEmail) {
        if (requesterEmail == null || requesterEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        String email = requesterEmail.trim().toLowerCase();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String generateTicketCode() {
        byte[] bytes = new byte[9];
        SECURE_RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "TCK-" + raw;
    }

    private String generatePaymentTransactionId() {
        byte[] bytes = new byte[12];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
