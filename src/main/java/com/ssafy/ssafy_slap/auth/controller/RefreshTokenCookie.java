package com.ssafy.ssafy_slap.auth.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class RefreshTokenCookie {

    public static final String NAME = "slap_refresh_token";
    private static final String PATH = "/api/auth";

    private final boolean secure;
    private final String sameSite;

    public RefreshTokenCookie(
            @Value("${auth.refresh-cookie-secure:true}") boolean secure,
            @Value("${auth.refresh-cookie-same-site:None}") String sameSite
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public ResponseCookie create(String token, Instant expiresAt) {
        Duration maxAge = Duration.between(Instant.now(), expiresAt);
        return base(token)
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build();
    }

    public ResponseCookie expire() {
        return base("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(PATH);
    }
}
