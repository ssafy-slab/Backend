package com.ssafy.ssafy_slap.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void createsAndParsesAccessToken() {
        JwtTokenProvider tokenProvider = new JwtTokenProvider(
                "test-secret-key-that-is-long-enough-for-hmac",
                3_600_000L
        );

        String token = tokenProvider.createAccessToken(7L, "USER");

        JwtPrincipal principal = tokenProvider.parse(token);
        assertThat(principal.userId()).isEqualTo(7L);
        assertThat(principal.role()).isEqualTo("USER");
    }
}
