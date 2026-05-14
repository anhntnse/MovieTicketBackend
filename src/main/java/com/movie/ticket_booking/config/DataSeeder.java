package com.movie.ticket_booking.config;

import com.movie.ticket_booking.entity.Room;
import com.movie.ticket_booking.entity.Seat;
import com.movie.ticket_booking.entity.Showtime;
import com.movie.ticket_booking.entity.Movie;
import com.movie.ticket_booking.entity.Genre;
import com.movie.ticket_booking.entity.Role;
import com.movie.ticket_booking.entity.User;
import com.movie.ticket_booking.entity.enums.AgeRating;
import com.movie.ticket_booking.entity.enums.MovieFormat;
import com.movie.ticket_booking.entity.enums.MovieStatus;
import com.movie.ticket_booking.entity.enums.RoleName;
import com.movie.ticket_booking.entity.enums.SeatType;
import com.movie.ticket_booking.repository.GenreRepository;
import com.movie.ticket_booking.repository.MovieRepository;
import com.movie.ticket_booking.repository.RoleRepository;
import com.movie.ticket_booking.repository.RoomRepository;
import com.movie.ticket_booking.repository.ShowtimeRepository;
import com.movie.ticket_booking.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements ApplicationRunner {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            GenreRepository genreRepository,
            MovieRepository movieRepository,
            RoomRepository roomRepository,
            ShowtimeRepository showtimeRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedAdminUser();
        seedGenres();
        seedMovies();
        seedRooms();
        seedShowtimes();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@gmail.com";
        String adminUsername = "admin";
        String adminPassword = "admin@123";

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        userRepository.findByEmail(adminEmail).ifPresentOrElse(existing -> {
            if (!existing.getRoles().contains(adminRole)) {
                existing.getRoles().add(adminRole);
                userRepository.save(existing);
            }
        }, () -> {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .enabled(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(admin);
        });
    }

    private void seedGenres() {
        List<GenreSeed> seeds = List.of(
                new GenreSeed("Hành động", "hanh-dong"),
                new GenreSeed("Hài", "hai"),
                new GenreSeed("Tâm lý", "tam-ly"),
                new GenreSeed("Kinh dị", "kinh-di"),
                new GenreSeed("Tình cảm", "tinh-cam"),
                new GenreSeed("Khoa học viễn tưởng", "sci-fi")
        );

        for (GenreSeed seed : seeds) {
            genreRepository.findBySlug(seed.slug()).orElseGet(() ->
                    genreRepository.save(Genre.builder().name(seed.name()).slug(seed.slug()).build())
            );
        }
    }

    private record GenreSeed(String name, String slug) {
    }

    private void seedMovies() {
        List<MovieSeed> seeds = List.of(
                new MovieSeed(
                        "Hành Tinh Cát: Phần Hai",
                        "hanh-tinh-cat-phan-hai",
                        "https://example.com/posters/dune-2.jpg",
                        AgeRating.T16,
                        MovieStatus.NOW_SHOWING,
                        Set.of(MovieFormat.IMAX, MovieFormat.THREE_D),
                        166,
                        LocalDate.of(2024, 3, 1),
                        "https://example.com/trailers/dune-2",
                        "Cuộc chiến trên Arrakis bước vào giai đoạn quyết định.",
                        new BigDecimal("4.6"),
                        List.of("Timothée Chalamet", "Zendaya", "Rebecca Ferguson"),
                        List.of("sci-fi", "hanh-dong")
                ),
                new MovieSeed(
                        "Cười Xuyên Màn Đêm",
                        "cuoi-xuyen-man-dem",
                        "https://example.com/posters/comedy-night.jpg",
                        AgeRating.T13,
                        MovieStatus.NOW_SHOWING,
                        Set.of(MovieFormat.TWO_D),
                        98,
                        LocalDate.of(2025, 1, 20),
                        null,
                        "Một đêm náo loạn với những tình huống dở khóc dở cười.",
                        new BigDecimal("4.1"),
                        List.of("Diễn viên A", "Diễn viên B"),
                        List.of("hai", "tinh-cam")
                ),
                new MovieSeed(
                        "Bóng Tối Trong Gương",
                        "bong-toi-trong-guong",
                        "https://example.com/posters/mirror-darkness.jpg",
                        AgeRating.T18,
                        MovieStatus.COMING_SOON,
                        Set.of(MovieFormat.TWO_D),
                        112,
                        LocalDate.of(2026, 6, 15),
                        "https://example.com/trailers/mirror-darkness",
                        "Khi chiếc gương phản chiếu một bí mật không nên được nhìn thấy.",
                        new BigDecimal("4.3"),
                        List.of("Diễn viên C", "Diễn viên D"),
                        List.of("kinh-di", "tam-ly")
                )
        );

        for (MovieSeed seed : seeds) {
            movieRepository.findBySlug(seed.slug()).orElseGet(() -> {
                Set<Genre> genres = new HashSet<>();
                for (String genreSlug : seed.genreSlugs()) {
                    Genre genre = genreRepository.findBySlug(genreSlug)
                            .orElseGet(() -> genreRepository.save(Genre.builder().name(genreSlug).slug(genreSlug).build()));
                    genres.add(genre);
                }

                Movie movie = Movie.builder()
                        .title(seed.title())
                        .slug(seed.slug())
                        .posterUrl(seed.posterUrl())
                        .ageRating(seed.ageRating())
                        .status(seed.status())
                        .formats(new HashSet<>(seed.formats()))
                        .duration(seed.duration())
                        .releaseDate(seed.releaseDate())
                        .trailerUrl(seed.trailerUrl())
                        .description(seed.description())
                        .ratingScore(seed.ratingScore())
                        .cast(seed.cast())
                        .genres(genres)
                        .build();

                return movieRepository.save(movie);
            });
        }
    }

    private record MovieSeed(
            String title,
            String slug,
            String posterUrl,
            AgeRating ageRating,
            MovieStatus status,
            Set<MovieFormat> formats,
            Integer duration,
            LocalDate releaseDate,
            String trailerUrl,
            String description,
            BigDecimal ratingScore,
            List<String> cast,
            List<String> genreSlugs
    ) {
    }

    private void seedRooms() {
        List<RoomSeed> seeds = List.of(
                new RoomSeed("R1", "Phòng 1", 6, 10),
                new RoomSeed("R2", "Phòng 2", 8, 12)
        );

        for (RoomSeed seed : seeds) {
            roomRepository.findByCode(seed.code()).orElseGet(() -> {
                Room room = Room.builder()
                        .code(seed.code())
                        .name(seed.name())
                        .totalRows(seed.totalRows())
                        .totalColumns(seed.totalColumns())
                        .build();

                generateSeats(room);
                return roomRepository.save(room);
            });
        }
    }

    private record RoomSeed(String code, String name, Integer totalRows, Integer totalColumns) {
    }

    private void generateSeats(Room room) {
        int totalRows = room.getTotalRows();
        if (totalRows > 26) {
            throw new IllegalArgumentException("totalRows must be <= 26");
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

    private void seedShowtimes() {
        Room room1 = roomRepository.findByCode("R1").orElse(null);
        Room room2 = roomRepository.findByCode("R2").orElse(null);

        Movie dune2 = movieRepository.findBySlug("hanh-tinh-cat-phan-hai").orElse(null);
        Movie comedy = movieRepository.findBySlug("cuoi-xuyen-man-dem").orElse(null);

        if (room1 == null || room2 == null || dune2 == null || comedy == null) {
            return;
        }

        List<ShowtimeSeed> seeds = List.of(
                new ShowtimeSeed(dune2, room1, Instant.parse("2026-05-14T03:00:00Z"), Instant.parse("2026-05-14T05:50:00Z"), new BigDecimal("120000")),
                new ShowtimeSeed(comedy, room1, Instant.parse("2026-05-14T06:30:00Z"), Instant.parse("2026-05-14T08:15:00Z"), new BigDecimal("90000")),
                new ShowtimeSeed(dune2, room2, Instant.parse("2026-05-14T03:30:00Z"), Instant.parse("2026-05-14T06:20:00Z"), new BigDecimal("110000"))
        );

        for (ShowtimeSeed seed : seeds) {
            showtimeRepository.findByRoomIdAndStartTime(seed.room().getId(), seed.startTime())
                    .orElseGet(() -> showtimeRepository.save(Showtime.builder()
                            .movie(seed.movie())
                            .room(seed.room())
                            .startTime(seed.startTime())
                            .endTime(seed.endTime())
                            .basePrice(seed.basePrice())
                            .build()));
        }
    }

    private record ShowtimeSeed(Movie movie, Room room, Instant startTime, Instant endTime, BigDecimal basePrice) {
    }
}
