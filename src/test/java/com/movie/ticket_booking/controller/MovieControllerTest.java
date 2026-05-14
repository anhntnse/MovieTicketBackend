package com.movie.ticket_booking.controller;

import com.movie.ticket_booking.config.SecurityConfig;
import com.movie.ticket_booking.dto.MovieResponse;
import com.movie.ticket_booking.entity.enums.AgeRating;
import com.movie.ticket_booking.entity.enums.MovieStatus;
import com.movie.ticket_booking.service.MovieService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    void getAllMovies_returnsList() throws Exception {
        MovieResponse movie = MovieResponse.builder()
                .id(UUID.randomUUID())
                .title("Lật mặt 7")
                .slug("lat-mat-7")
                .posterUrl("https://example.com/poster.jpg")
                .ageRating(AgeRating.T16)
                .status(MovieStatus.NOW_SHOWING)
                .duration(120)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .cast(List.of())
                .formats(java.util.Set.of())
                .genres(List.of())
                .build();

        when(movieService.getAll()).thenReturn(List.of(movie));

        mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Lật mặt 7"))
                .andExpect(jsonPath("$[0].slug").value("lat-mat-7"));
    }

            @Test
            void getMovieById_returnsMovie() throws Exception {
            UUID id = UUID.randomUUID();
            MovieResponse movie = MovieResponse.builder()
                .id(id)
                .title("Lật mặt 7")
                .slug("lat-mat-7")
                .posterUrl("https://example.com/poster.jpg")
                .ageRating(AgeRating.T16)
                .status(MovieStatus.NOW_SHOWING)
                .duration(120)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .cast(List.of())
                .formats(java.util.Set.of())
                .genres(List.of())
                .build();

            when(movieService.getById(id)).thenReturn(movie);

            mockMvc.perform(get("/api/movies/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.slug").value("lat-mat-7"));
            }

            @Test
            void createMovie_returns201AndBody() throws Exception {
            UUID id = UUID.randomUUID();
            MovieResponse created = MovieResponse.builder()
                .id(id)
                .title("Lật mặt 7")
                .slug("lat-mat-7")
                .posterUrl("https://example.com/poster.jpg")
                .ageRating(AgeRating.T16)
                .status(MovieStatus.NOW_SHOWING)
                .duration(120)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .cast(List.of())
                .formats(java.util.Set.of())
                .genres(List.of())
                .build();

            when(movieService.create(any())).thenReturn(created);

            String payload = "{" +
                "\"title\":\"Lật mặt 7\"," +
                "\"slug\":\"lat-mat-7\"," +
                "\"posterUrl\":\"https://example.com/poster.jpg\"," +
                "\"ageRating\":\"T16\"," +
                "\"status\":\"NOW_SHOWING\"," +
                "\"duration\":120" +
                "}";

            mockMvc.perform(post("/api/movies")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/movies/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.slug").value("lat-mat-7"));
            }

            @Test
            void createMovie_withoutToken_returns401() throws Exception {
            String payload = "{" +
                "\"title\":\"Lật mặt 7\"," +
                "\"slug\":\"lat-mat-7\"," +
                "\"posterUrl\":\"https://example.com/poster.jpg\"," +
                "\"ageRating\":\"T16\"," +
                "\"status\":\"NOW_SHOWING\"," +
                "\"duration\":120" +
                "}";

            mockMvc.perform(post("/api/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isUnauthorized());
            }

            @Test
            void createMovie_withUserRole_returns403() throws Exception {
            String payload = "{" +
                "\"title\":\"Lật mặt 7\"," +
                "\"slug\":\"lat-mat-7\"," +
                "\"posterUrl\":\"https://example.com/poster.jpg\"," +
                "\"ageRating\":\"T16\"," +
                "\"status\":\"NOW_SHOWING\"," +
                "\"duration\":120" +
                "}";

            mockMvc.perform(post("/api/movies")
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isForbidden());
            }

            @Test
            void updateMovie_returns200AndBody() throws Exception {
            UUID id = UUID.randomUUID();
            MovieResponse updated = MovieResponse.builder()
                .id(id)
                .title("Lật mặt 7 (Updated)")
                .slug("lat-mat-7")
                .posterUrl("https://example.com/poster.jpg")
                .ageRating(AgeRating.T16)
                .status(MovieStatus.NOW_SHOWING)
                .duration(121)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .cast(List.of())
                .formats(java.util.Set.of())
                .genres(List.of())
                .build();

            when(movieService.update(any(), any())).thenReturn(updated);

            String payload = "{" +
                "\"title\":\"Lật mặt 7 (Updated)\"," +
                "\"slug\":\"lat-mat-7\"," +
                "\"posterUrl\":\"https://example.com/poster.jpg\"," +
                "\"ageRating\":\"T16\"," +
                "\"status\":\"NOW_SHOWING\"," +
                "\"duration\":121" +
                "}";

            mockMvc.perform(put("/api/movies/{id}", id)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Lật mặt 7 (Updated)"));
            }

            @Test
            void deleteMovie_returns204() throws Exception {
            UUID id = UUID.randomUUID();
            mockMvc.perform(delete("/api/movies/{id}", id)
                    .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
            }
}
