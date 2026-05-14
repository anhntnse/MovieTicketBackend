package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.config.SecurityConfig;
import com.movie.ticket_booking.dto.RoomResponse;
import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.entity.enums.SeatType;
import com.movie.ticket_booking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@Import(SecurityConfig.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Test
    void getAllRooms_returnsList() throws Exception {
        RoomResponse room = RoomResponse.builder()
                .id(1L)
                .code("R1")
                .name("Phòng 1")
                .totalRows(6)
                .totalColumns(10)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(roomService.getAll()).thenReturn(List.of(room));

        mockMvc.perform(get("/api/rooms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("R1"));
    }

    @Test
    void getRoomById_returnsRoom() throws Exception {
        RoomResponse room = RoomResponse.builder()
                .id(1L)
                .code("R1")
                .name("Phòng 1")
                .totalRows(6)
                .totalColumns(10)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(roomService.getById(1L)).thenReturn(room);

        mockMvc.perform(get("/api/rooms/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("R1"));
    }

    @Test
    void createRoom_returns201() throws Exception {
        RoomResponse created = RoomResponse.builder()
                .id(2L)
                .code("R2")
                .name("Phòng 2")
                .totalRows(8)
                .totalColumns(12)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(roomService.create(any())).thenReturn(created);

        String payload = "{" +
                "\"code\":\"R2\"," +
                "\"name\":\"Phòng 2\"," +
                "\"totalRows\":8," +
                "\"totalColumns\":12" +
                "}";

        mockMvc.perform(post("/api/rooms")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/rooms/" + created.getId()))
                .andExpect(jsonPath("$.code").value("R2"));
    }

    @Test
    void updateRoom_returns200() throws Exception {
        RoomResponse updated = RoomResponse.builder()
                .id(1L)
                .code("R1")
                .name("Phòng 1 (Updated)")
                .totalRows(6)
                .totalColumns(10)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(roomService.update(eq(1L), any())).thenReturn(updated);

        String payload = "{" +
                "\"code\":\"R1\"," +
                "\"name\":\"Phòng 1 (Updated)\"," +
                "\"totalRows\":6," +
                "\"totalColumns\":10" +
                "}";

        mockMvc.perform(put("/api/rooms/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phòng 1 (Updated)"));
    }

    @Test
    void deleteRoom_returns204() throws Exception {
        mockMvc.perform(delete("/api/rooms/{id}", 1L)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRoomSeats_returnsList() throws Exception {
        SeatResponse seat = SeatResponse.builder()
                .id(10L)
                .label("A1")
                .rowNumber(1)
                .columnNumber(1)
                .type(SeatType.NORMAL)
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(roomService.getSeats(1L)).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/rooms/{id}/seats", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("A1"));
    }
}
