package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.domain.RefreshTokenSession;
import com.ssafy.ssafy_slap.auth.mapper.RefreshTokenMapper;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-24T00:00:00Z");

    @Test
    void issuesOpaqueRefreshTokenAndStoresOnlyItsHash() {
        RefreshTokenMapper mapper = mock(RefreshTokenMapper.class);
        RefreshTokenService service = service(mapper, mock(UserMapper.class));

        IssuedRefreshToken issued = service.issue(7L);

        assertThat(issued.token()).isNotBlank();
        assertThat(issued.expiresAt()).isEqualTo(NOW.plusSeconds(14 * 24 * 60 * 60));
        verify(mapper).insert(7L, RefreshTokenService.sha256(issued.token()), issued.expiresAt());
    }

    @Test
    void rotatesRefreshTokenAndRevokesPreviousSession() {
        RefreshTokenMapper mapper = mock(RefreshTokenMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        RefreshTokenService service = service(mapper, userMapper);
        AppUser user = activeUser(7L);
        RefreshTokenSession current = new RefreshTokenSession(
                31L,
                7L,
                RefreshTokenService.sha256("old-refresh-token"),
                NOW.plusSeconds(3600),
                null
        );
        when(mapper.findByTokenHash(current.tokenHash())).thenReturn(Optional.of(current));
        when(userMapper.findActiveById(7L)).thenReturn(Optional.of(user));

        RefreshRotation rotation = service.rotate("old-refresh-token");

        verify(mapper).revoke(31L, NOW);
        verify(mapper).insert(7L, RefreshTokenService.sha256(rotation.refreshToken().token()),
                rotation.refreshToken().expiresAt());
        assertThat(rotation.user()).isSameAs(user);
        assertThat(rotation.refreshToken().token()).isNotEqualTo("old-refresh-token");
    }

    @Test
    void rejectsReusedRevokedTokenAndRevokesAllUserSessions() {
        RefreshTokenMapper mapper = mock(RefreshTokenMapper.class);
        RefreshTokenService service = service(mapper, mock(UserMapper.class));
        RefreshTokenSession revoked = new RefreshTokenSession(
                31L,
                7L,
                RefreshTokenService.sha256("reused-token"),
                NOW.plusSeconds(3600),
                NOW.minusSeconds(1)
        );
        when(mapper.findByTokenHash(revoked.tokenHash())).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.rotate("reused-token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(mapper).revokeAllByUserId(7L, NOW);
    }

    private RefreshTokenService service(RefreshTokenMapper mapper, UserMapper userMapper) {
        return new RefreshTokenService(
                mapper,
                userMapper,
                14 * 24 * 60 * 60 * 1000L,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private AppUser activeUser(Long userId) {
        return new AppUser(
                userId,
                "test@example.com",
                "encoded",
                "tester",
                "USER",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
