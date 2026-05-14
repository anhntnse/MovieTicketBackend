package com.movie.ticket_booking.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotNull
    private Long showtimeId;

    @NotEmpty
    private List<@NotNull Long> seatIds;

    @NotNull
    @Email
    @Size(max = 320)
    private String contactEmail;

    @Size(max = 64)
    private String paymentProvider;
}
