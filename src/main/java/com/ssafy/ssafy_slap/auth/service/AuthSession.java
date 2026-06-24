package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;

public record AuthSession(AuthResponse response, IssuedRefreshToken refreshToken) {

    public String tokenType() {
        return response.tokenType();
    }

    public String accessToken() {
        return response.accessToken();
    }

    public AuthUserResponse user() {
        return response.user();
    }
}
