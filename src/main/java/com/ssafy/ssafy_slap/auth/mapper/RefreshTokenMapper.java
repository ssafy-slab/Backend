package com.ssafy.ssafy_slap.auth.mapper;

import com.ssafy.ssafy_slap.auth.domain.RefreshTokenSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {

    void insert(
            @Param("userId") Long userId,
            @Param("tokenHash") String tokenHash,
            @Param("expiresAt") Instant expiresAt
    );

    Optional<RefreshTokenSession> findByTokenHash(@Param("tokenHash") String tokenHash);

    void revoke(@Param("refreshTokenId") Long refreshTokenId, @Param("revokedAt") Instant revokedAt);

    void revokeAllByUserId(@Param("userId") Long userId, @Param("revokedAt") Instant revokedAt);
}
