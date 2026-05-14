package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.config.SecurityConfig;
import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.entity.enums.SeatType;
import com.movie.ticket_booking.service.SeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatController.class)
@Import(SecurityConfig.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeatService seatService;

    @Test
    void patchSeat_updatesTypeAndActive() throws Exception {
        SeatResponse updated = SeatResponse.builder()
                .id(10L)
                .label("A1")
                .rowNumber(1)
                .columnNumber(1)
                .type(SeatType.VIP)
                .active(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(seatService.update(eq(10L), any())).thenReturn(updated);

        String payload = "{" +
                "\"type\":\"VIP\"," +
                "\"active\":false" +
                "}";

        mockMvc.perform(patch("/api/seats/{id}", 10L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("VIP"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void patchSeat_withUserRole_returns403() throws Exception {
        String payload = "{" +
                "\"type\":\"VIP\"," +
                "\"active\":false" +
                "}";

        mockMvc.perform(patch("/api/seats/{id}", 10L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }
}
