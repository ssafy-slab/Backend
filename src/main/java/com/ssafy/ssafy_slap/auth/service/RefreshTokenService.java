package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.domain.RefreshTokenSession;
import com.ssafy.ssafy_slap.auth.mapper.RefreshTokenMapper;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;
    private final long validityMillis;
    private final Clock clock;
    private final SecureRandom secureRandom;

    @Autowired
    public RefreshTokenService(
            RefreshTokenMapper refreshTokenMapper,
            UserMapper userMapper,
            @Value("${jwt.refresh-token-validity-millis}") long validityMillis
    ) {
        this(refreshTokenMapper, userMapper, validityMillis, Clock.systemUTC(), new SecureRandom());
    }

    RefreshTokenService(
            RefreshTokenMapper refreshTokenMapper,
            UserMapper userMapper,
            long validityMillis,
            Clock clock
    ) {
        this(refreshTokenMapper, userMapper, validityMillis, clock, new SecureRandom());
    }

    RefreshTokenService(
            RefreshTokenMapper refreshTokenMapper,
            UserMapper userMapper,
            long validityMillis,
            Clock clock,
            SecureRandom secureRandom
    ) {
        this.refreshTokenMapper = refreshTokenMapper;
        this.userMapper = userMapper;
        this.validityMillis = validityMillis;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public IssuedRefreshToken issue(Long userId) {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plusMillis(validityMillis);
        refreshTokenMapper.insert(userId, sha256(token), expiresAt);
        return new IssuedRefreshToken(token, expiresAt);
    }

    @Transactional
    public RefreshRotation rotate(String token) {
        RefreshTokenSession session = findSession(token);
        Instant now = clock.instant();
        if (session.revokedAt() != null) {
            refreshTokenMapper.revokeAllByUserId(session.userId(), now);
            throw invalidToken();
        }
        if (!session.expiresAt().isAfter(now)) {
            refreshTokenMapper.revoke(session.refreshTokenId(), now);
            throw invalidToken();
        }

        AppUser user = userMapper.findActiveById(session.userId())
                .orElseThrow(() -> {
                    refreshTokenMapper.revokeAllByUserId(session.userId(), now);
                    return invalidToken();
                });

        refreshTokenMapper.revoke(session.refreshTokenId(), now);
        return new RefreshRotation(user, issue(user.getUserId()));
    }

    @Transactional
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        refreshTokenMapper.findByTokenHash(sha256(token))
                .ifPresent(session -> refreshTokenMapper.revoke(session.refreshTokenId(), clock.instant()));
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenMapper.revokeAllByUserId(userId, clock.instant());
    }

    private RefreshTokenSession findSession(String token) {
        if (token == null || token.isBlank()) {
            throw invalidToken();
        }
        return refreshTokenMapper.findByTokenHash(sha256(token))
                .orElseThrow(this::invalidToken);
    }

    static String sha256(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash refresh token", exception);
        }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
}
