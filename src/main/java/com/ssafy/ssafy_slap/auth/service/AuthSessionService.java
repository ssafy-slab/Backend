package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import org.springframework.stereotype.Service;

@Service
public class AuthSessionService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthSessionService(JwtTokenProvider tokenProvider, RefreshTokenService refreshTokenService) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthSession create(AppUser user) {
        AuthResponse response = new AuthResponse(
                "Bearer",
                tokenProvider.createAccessToken(user.getUserId(), user.getRole()),
                AuthUserResponse.from(user)
        );
        return new AuthSession(response, refreshTokenService.issue(user.getUserId()));
    }

    public AuthSession refresh(String refreshToken) {
        RefreshRotation rotation = refreshTokenService.rotate(refreshToken);
        AppUser user = rotation.user();
        AuthResponse response = new AuthResponse(
                "Bearer",
                tokenProvider.createAccessToken(user.getUserId(), user.getRole()),
                AuthUserResponse.from(user)
        );
        return new AuthSession(response, rotation.refreshToken());
    }
}
