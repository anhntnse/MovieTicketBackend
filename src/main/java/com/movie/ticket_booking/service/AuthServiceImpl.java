package com.movie.ticket_booking.service;

import com.movie.ticket_booking.dto.auth.*;
import com.movie.ticket_booking.entity.RefreshToken;
import com.movie.ticket_booking.entity.Role;
import com.movie.ticket_booking.entity.User;
import com.movie.ticket_booking.entity.enums.RoleName;
import com.movie.ticket_booking.repository.RefreshTokenRepository;
import com.movie.ticket_booking.repository.RoleRepository;
import com.movie.ticket_booking.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${app.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Override
    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

        User user = User.builder()
                .username(request.getUsername())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        User created = userRepository.save(user);
        log.info("Registered new user id={} email={}", created.getId(), created.getEmail());
        return toUserResponse(created);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Instant now = Instant.now();
        String accessToken = createAccessToken(user, now);
        RefreshToken refreshToken = createRefreshToken(user, now);
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresInSeconds(accessTtlSeconds)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        Instant now = Instant.now();
        RefreshToken current = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (current.getRevokedAt() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked");
        }
        if (current.getExpiresAt().isBefore(now)) {
            current.setRevokedAt(now);
            refreshTokenRepository.save(current);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        User user = current.getUser();

        current.setRevokedAt(now);
        refreshTokenRepository.save(current);

        String newAccessToken = createAccessToken(user, now);
        RefreshToken nextRefreshToken = createRefreshToken(user, now);
        refreshTokenRepository.save(nextRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(nextRefreshToken.getToken())
                .expiresInSeconds(accessTtlSeconds)
                .user(toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        Instant now = Instant.now();
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.getRevokedAt() == null) {
            token.setRevokedAt(now);
            refreshTokenRepository.save(token);
        }
    }

    private String createAccessToken(User user, Instant now) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .sorted()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ticket-booking")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTtlSeconds))
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private RefreshToken createRefreshToken(User user, Instant now) {
        return RefreshToken.builder()
                .token(generateRefreshTokenValue())
                .user(user)
                .expiresAt(now.plusSeconds(refreshTtlSeconds))
                .build();
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private AuthUserResponse toUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .sorted()
                .toList();

        return AuthUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
