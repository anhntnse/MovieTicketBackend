package com.movie.ticket_booking.dto.auth;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserResponse {

    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}
