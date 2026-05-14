package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.RoomRequest;
import com.movie.ticket_booking.dto.RoomResponse;
import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.service.RoomService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Slf4j
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> getAllRooms() {
        log.info("GET /api/rooms");
        List<RoomResponse> result = roomService.getAll();
        log.info("GET /api/rooms -> {} item(s)", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public RoomResponse getRoomById(@PathVariable Long id) {
        log.info("GET /api/rooms/{}", id);
        RoomResponse result = roomService.getById(id);
        log.info("GET /api/rooms/{} -> code={}", id, result.getCode());
        return result;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        log.info("POST /api/rooms code={} name={} rows={} cols={}", request.getCode(), request.getName(), request.getTotalRows(), request.getTotalColumns());
        RoomResponse created = roomService.create(request);
        log.info("POST /api/rooms -> 201 id={} code={}", created.getId(), created.getCode());
        return ResponseEntity.created(URI.create("/api/rooms/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        log.info("PUT /api/rooms/{} code={} name={}", id, request.getCode(), request.getName());
        RoomResponse updated = roomService.update(id, request);
        log.info("PUT /api/rooms/{} -> 200 code={}", id, updated.getCode());
        return updated;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        log.info("DELETE /api/rooms/{}", id);
        roomService.delete(id);
        log.info("DELETE /api/rooms/{} -> 204", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public List<SeatResponse> getRoomSeats(@PathVariable Long id) {
        log.info("GET /api/rooms/{}/seats", id);
        List<SeatResponse> result = roomService.getSeats(id);
        log.info("GET /api/rooms/{}/seats -> {} item(s)", id, result.size());
        return result;
    }
}
