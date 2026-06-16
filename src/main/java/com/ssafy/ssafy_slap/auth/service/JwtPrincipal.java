package com.ssafy.ssafy_slap.auth.service;

public record JwtPrincipal(
        Long userId,
        String role
) {
}
