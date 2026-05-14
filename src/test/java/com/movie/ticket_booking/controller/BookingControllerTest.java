package com.movie.ticket_booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.ticket_booking.dto.booking.CheckoutRequest;
import com.movie.ticket_booking.dto.booking.HoldSeatsRequest;
import com.movie.ticket_booking.entity.Role;
import com.movie.ticket_booking.entity.Showtime;
import com.movie.ticket_booking.entity.User;
import com.movie.ticket_booking.entity.enums.RoleName;
import com.movie.ticket_booking.repository.RoleRepository;
import com.movie.ticket_booking.repository.ShowtimeRepository;
import com.movie.ticket_booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Test
    void hold_conflict_between_two_users_returns409() throws Exception {
                Showtime showtime = getOrMakeFutureShowtime();

        // Parse JSON properly
        String seatsJson = mockMvc.perform(get("/api/showtimes/{id}/seats", showtime.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> seats = objectMapper.readValue(seatsJson, List.class);
        Long firstActiveSeatId = null;
        for (Map<String, Object> s : seats) {
            Boolean active = (Boolean) s.get("active");
            String status = (String) s.get("status");
            if (Boolean.TRUE.equals(active) && "AVAILABLE".equals(status)) {
                firstActiveSeatId = ((Number) s.get("seatId")).longValue();
                break;
            }
        }
        if (firstActiveSeatId == null) {
            throw new IllegalStateException("No available seat found for test");
        }

        String u1 = "user1_hold@gmail.com";
        String u2 = "user2_hold@gmail.com";
        seedUserIfMissing(u1, "user1_hold");
        seedUserIfMissing(u2, "user2_hold");

        HoldSeatsRequest hold = HoldSeatsRequest.builder()
                .showtimeId(showtime.getId())
                .seatIds(List.of(firstActiveSeatId))
                .build();

        mockMvc.perform(post("/api/bookings/holds")
                        .with(jwt().jwt(j -> j.subject(u1)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatId").value(firstActiveSeatId));

        mockMvc.perform(post("/api/bookings/holds")
                        .with(jwt().jwt(j -> j.subject(u2)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hold)))
                .andExpect(status().isConflict());
    }

    @Test
    void checkout_requires_hold_and_creates_tickets() throws Exception {
                Showtime showtime = getOrMakeFutureShowtime();

        String seatsJson = mockMvc.perform(get("/api/showtimes/{id}/seats", showtime.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> seats = objectMapper.readValue(seatsJson, List.class);
        Long seatId = null;
        for (Map<String, Object> s : seats) {
            Boolean active = (Boolean) s.get("active");
            String status = (String) s.get("status");
            if (Boolean.TRUE.equals(active) && "AVAILABLE".equals(status)) {
                seatId = ((Number) s.get("seatId")).longValue();
                break;
            }
        }
        if (seatId == null) {
            throw new IllegalStateException("No available seat found for test");
        }

        String email = "buyer@gmail.com";
        seedUserIfMissing(email, "buyer");

        HoldSeatsRequest hold = HoldSeatsRequest.builder()
                .showtimeId(showtime.getId())
                .seatIds(List.of(seatId))
                .build();

        mockMvc.perform(post("/api/bookings/holds")
                        .with(jwt().jwt(j -> j.subject(email)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hold)))
                .andExpect(status().isOk());

        CheckoutRequest checkout = CheckoutRequest.builder()
                .showtimeId(showtime.getId())
                .seatIds(List.of(seatId))
                .contactEmail(email)
                .paymentProvider("TEST")
                .build();

        mockMvc.perform(post("/api/bookings/checkout")
                        .with(jwt().jwt(j -> j.subject(email)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkout)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").isNumber())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.tickets[0].seatId").value(seatId.intValue()));
    }

    private void seedUserIfMissing(String email, String username) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        User user = User.builder()
                .email(email)
                .username(username)
                .enabled(true)
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(java.util.Set.of(userRole))
                .build();
        userRepository.save(user);
    }

        private Showtime getOrMakeFutureShowtime() {
                Showtime showtime = showtimeRepository.findAll().stream().findFirst().orElseThrow();
                if (!java.time.Instant.now().isBefore(showtime.getStartTime())) {
                        showtime.setStartTime(java.time.Instant.now().plusSeconds(3600));
                        showtimeRepository.save(showtime);
                }
                return showtime;
        }
}
