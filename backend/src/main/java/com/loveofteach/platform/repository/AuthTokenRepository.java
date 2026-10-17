package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("""
            select token from AuthToken token
            join fetch token.user user
            left join fetch user.teacher
            where token.token = :token and token.expiresAt > :now and user.active = true
            """)
    Optional<AuthToken> findActiveToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now);

    void deleteByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
