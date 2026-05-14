package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.RoomRequest;
import com.movie.ticket_booking.dto.RoomResponse;
import com.movie.ticket_booking.dto.SeatResponse;
import com.movie.ticket_booking.entity.Room;
import com.movie.ticket_booking.entity.Seat;
import com.movie.ticket_booking.entity.enums.SeatType;
import com.movie.ticket_booking.repository.RoomRepository;
import com.movie.ticket_booking.repository.SeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;

    public RoomServiceImpl(RoomRepository roomRepository, SeatRepository seatRepository) {
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAll() {
        return roomRepository.findAll().stream()
                .sorted(Comparator.comparing(Room::getCode, String::compareToIgnoreCase))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        return toResponse(room);
    }

    @Override
    public RoomResponse create(RoomRequest request) {
        roomRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Room code already exists");
                });

        Room room = Room.builder()
                .code(request.getCode())
                .name(request.getName())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .build();

        generateSeats(room);

        Room saved = roomRepository.saveAndFlush(room);
        return toResponse(saved);
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (!room.getCode().equals(request.getCode())) {
            roomRepository.findByCode(request.getCode())
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Room code already exists");
                    });
            room.setCode(request.getCode());
        }

        room.setName(request.getName());

        if (!room.getTotalRows().equals(request.getTotalRows()) || !room.getTotalColumns().equals(request.getTotalColumns())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change totalRows/totalColumns after seats are created");
        }

        Room saved = roomRepository.saveAndFlush(room);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        roomRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        return seatRepository.findByRoomIdOrderByRowNumberAscColumnNumberAsc(roomId)
                .stream()
                .map(this::toSeatResponse)
                .toList();
    }

    private void generateSeats(Room room) {
        int totalRows = room.getTotalRows();
        if (totalRows > 26) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "totalRows must be <= 26");
        }

        for (int row = 1; row <= room.getTotalRows(); row++) {
            char rowChar = (char) ('A' + (row - 1));
            for (int col = 1; col <= room.getTotalColumns(); col++) {
                Seat seat = Seat.builder()
                        .room(room)
                        .label(rowChar + String.valueOf(col))
                        .rowNumber(row)
                        .columnNumber(col)
                        .type(SeatType.NORMAL)
                        .active(true)
                        .build();
                room.getSeats().add(seat);
            }
        }
    }

    private RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .totalRows(room.getTotalRows())
                .totalColumns(room.getTotalColumns())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    private SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .label(seat.getLabel())
                .rowNumber(seat.getRowNumber())
                .columnNumber(seat.getColumnNumber())
                .type(seat.getType())
                .active(seat.isActive())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }
}
