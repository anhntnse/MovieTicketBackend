package com.movie.ticket_booking.repository;

import com.movie.ticket_booking.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken rt set rt.revokedAt = :now " +
            "where rt.user.id = :userId and rt.revokedAt is null and rt.expiresAt > :now")
    int revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
