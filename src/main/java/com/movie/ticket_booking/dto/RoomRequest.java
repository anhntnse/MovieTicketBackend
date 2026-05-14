package com.movie.ticket_booking.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {

    @NotBlank
    @Size(max = 20)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Positive
    @Max(26)
    private Integer totalRows;

    @NotNull
    @Positive
    private Integer totalColumns;
}
