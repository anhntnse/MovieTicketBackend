package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.config.SecurityConfig;
import com.movie.ticket_booking.dto.MovieSummaryResponse;
import com.movie.ticket_booking.dto.RoomSummaryResponse;
import com.movie.ticket_booking.dto.ShowtimeResponse;
import com.movie.ticket_booking.service.BookingService;
import com.movie.ticket_booking.service.ShowtimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShowtimeController.class)
@Import(SecurityConfig.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeService showtimeService;

        @MockBean
        private BookingService bookingService;

    @Test
    void getShowtimes_returnsList() throws Exception {
        ShowtimeResponse showtime = ShowtimeResponse.builder()
                .id(1L)
                .movie(MovieSummaryResponse.builder()
                        .id(UUID.randomUUID())
                        .title("Movie")
                        .slug("movie")
                        .posterUrl("https://example.com/poster.jpg")
                        .build())
                .room(RoomSummaryResponse.builder()
                        .id(1L)
                        .code("R1")
                        .name("Phòng 1")
                        .build())
                .startTime(Instant.parse("2026-05-14T03:00:00Z"))
                .endTime(Instant.parse("2026-05-14T05:00:00Z"))
                .basePrice(new BigDecimal("100000"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(showtimeService.getAll(null, null, null, null)).thenReturn(List.of(showtime));

        mockMvc.perform(get("/api/showtimes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].room.code").value("R1"));
    }

    @Test
    void getShowtimeById_returnsShowtime() throws Exception {
        ShowtimeResponse showtime = ShowtimeResponse.builder()
                .id(1L)
                .movie(MovieSummaryResponse.builder().id(UUID.randomUUID()).title("Movie").slug("movie").posterUrl("p").build())
                .room(RoomSummaryResponse.builder().id(1L).code("R1").name("Phòng 1").build())
                .startTime(Instant.parse("2026-05-14T03:00:00Z"))
                .endTime(Instant.parse("2026-05-14T05:00:00Z"))
                .basePrice(new BigDecimal("100000"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(showtimeService.getById(1L)).thenReturn(showtime);

        mockMvc.perform(get("/api/showtimes/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createShowtime_returns201() throws Exception {
        ShowtimeResponse created = ShowtimeResponse.builder()
                .id(2L)
                .movie(MovieSummaryResponse.builder().id(UUID.randomUUID()).title("Movie").slug("movie").posterUrl("p").build())
                .room(RoomSummaryResponse.builder().id(1L).code("R1").name("Phòng 1").build())
                .startTime(Instant.parse("2026-05-14T06:00:00Z"))
                .endTime(Instant.parse("2026-05-14T08:00:00Z"))
                .basePrice(new BigDecimal("120000"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(showtimeService.create(any())).thenReturn(created);

        String payload = "{" +
                "\"movieId\":\"" + created.getMovie().getId() + "\"," +
                "\"roomId\":1," +
                "\"startTime\":\"2026-05-14T06:00:00Z\"," +
                "\"endTime\":\"2026-05-14T08:00:00Z\"," +
                "\"basePrice\":120000" +
                "}";

        mockMvc.perform(post("/api/showtimes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/showtimes/" + created.getId()));
    }

    @Test
    void updateShowtime_returns200() throws Exception {
        ShowtimeResponse updated = ShowtimeResponse.builder()
                .id(2L)
                .movie(MovieSummaryResponse.builder().id(UUID.randomUUID()).title("Movie").slug("movie").posterUrl("p").build())
                .room(RoomSummaryResponse.builder().id(1L).code("R1").name("Phòng 1").build())
                .startTime(Instant.parse("2026-05-14T06:00:00Z"))
                .endTime(Instant.parse("2026-05-14T08:00:00Z"))
                .basePrice(new BigDecimal("130000"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(showtimeService.update(eq(2L), any())).thenReturn(updated);

        String payload = "{" +
                "\"movieId\":\"" + updated.getMovie().getId() + "\"," +
                "\"roomId\":1," +
                "\"startTime\":\"2026-05-14T06:00:00Z\"," +
                "\"endTime\":\"2026-05-14T08:00:00Z\"," +
                "\"basePrice\":130000" +
                "}";

        mockMvc.perform(put("/api/showtimes/{id}", 2L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice").value(130000));
    }

    @Test
    void deleteShowtime_returns204() throws Exception {
        mockMvc.perform(delete("/api/showtimes/{id}", 2L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}
