package com.ssafy.ssafy_slap.auth.controller;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieTest {

    @Test
    void createsHttpOnlySecureCrossSiteCookieWithoutExposingTokenToJavascript() {
        RefreshTokenCookie cookie = new RefreshTokenCookie(true, "None");

        String value = cookie.create("refresh-token", Instant.now().plusSeconds(3600)).toString();

        assertThat(value)
                .contains("slap_refresh_token=refresh-token")
                .contains("Path=/api/auth")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=None");
    }

    @Test
    void expiresRefreshCookieOnLogout() {
        RefreshTokenCookie cookie = new RefreshTokenCookie(true, "None");

        assertThat(cookie.expire().toString())
                .contains("slap_refresh_token=")
                .contains("Max-Age=0");
    }
}
