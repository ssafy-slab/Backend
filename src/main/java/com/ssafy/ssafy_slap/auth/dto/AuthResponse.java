package com.ssafy.ssafy_slap.auth.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        AuthUserResponse user
) {
}
