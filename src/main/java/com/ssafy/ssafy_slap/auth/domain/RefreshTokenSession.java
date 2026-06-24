package com.ssafy.ssafy_slap.auth.domain;

import java.time.Instant;

public record RefreshTokenSession(
        Long refreshTokenId,
        Long userId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt
) {
}
