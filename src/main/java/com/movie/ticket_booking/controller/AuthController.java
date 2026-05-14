package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.dto.auth.*;
import com.movie.ticket_booking.entity.User;
import com.movie.ticket_booking.service.AuthService;
import com.movie.ticket_booking.service.email.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register email={} username={}", request.getEmail(), request.getUsername());
        AuthUserResponse created = authService.register(request);
        emailService.sendWelcomeEmail(User.builder()
                .id(created.getId())
                .email(created.getEmail())
                .username(created.getUsername())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        log.info("POST /api/auth/login email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        if (response.getUser() != null) {
            emailService.sendLoginNotification(
                    User.builder()
                            .id(response.getUser().getId())
                            .email(response.getUser().getEmail())
                            .username(response.getUser().getUsername())
                            .build(),
                    Instant.now(),
                    httpServletRequest.getRemoteAddr(),
                    httpServletRequest.getHeader("User-Agent")
            );
        }
        return response;
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/auth/refresh");
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/auth/logout");
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AuthUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Number uid = jwt.getClaim("uid");
        Long userId = uid == null ? null : uid.longValue();
        List<String> roles = jwt.getClaimAsStringList("roles");
        return AuthUserResponse.builder()
            .id(userId)
                .username(jwt.getClaim("username"))
                .email(jwt.getSubject())
                .roles(roles == null ? List.of() : roles)
                .build();
    }
}
