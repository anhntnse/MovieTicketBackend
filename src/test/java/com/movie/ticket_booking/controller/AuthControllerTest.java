package com.movie.ticket_booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.ticket_booking.dto.auth.LoginRequest;
import com.movie.ticket_booking.dto.auth.RefreshTokenRequest;
import com.movie.ticket_booking.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_login_refresh_rotate_logout_flow() throws Exception {
        String email = "u_" + UUID.randomUUID() + "@gmail.com";
        String username = "u_" + UUID.randomUUID();
        String password = "password123";

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(email)
                .username(username)
                .password(password)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(email));

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String loginJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<?, ?> loginMap = objectMapper.readValue(loginJson, Map.class);
        String refreshToken1 = (String) loginMap.get("refreshToken");
        assertThat(refreshToken1).isNotBlank();

        RefreshTokenRequest refreshRequest1 = RefreshTokenRequest.builder()
                .refreshToken(refreshToken1)
                .build();

        String refreshJson = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<?, ?> refreshMap = objectMapper.readValue(refreshJson, Map.class);
        String refreshToken2 = (String) refreshMap.get("refreshToken");
        assertThat(refreshToken2).isNotBlank();
        assertThat(refreshToken2).isNotEqualTo(refreshToken1);

        // Old refresh token should now be revoked
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest1)))
                .andExpect(status().isUnauthorized());

        // Logout current token and verify it can't refresh
        RefreshTokenRequest logoutRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken2)
                .build();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@gmail.com")
                .password("wrong")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
