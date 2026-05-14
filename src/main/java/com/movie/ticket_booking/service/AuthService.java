package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.auth.*;

public interface AuthService {

    AuthUserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
