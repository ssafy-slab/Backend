package com.ssafy.ssafy_slap.global.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppFrontendPropertiesTest {

    @Test
    void parsesConfiguredFrontendOrigins() {
        AppFrontendProperties properties = new AppFrontendProperties(
                "http://localhost:5173, https://ssafyslap.vercel.app, http://localhost:5173",
                "https://ssafyslap.vercel.app"
        );

        assertThat(properties.allowedOriginPatterns())
                .containsExactly("http://localhost:5173", "https://ssafyslap.vercel.app");
    }

    @Test
    void buildsCallbackUriForAllowedOrigin() {
        AppFrontendProperties properties = new AppFrontendProperties(
                "http://localhost:5173,https://ssafyslap.vercel.app",
                "https://ssafyslap.vercel.app"
        );

        assertThat(properties.callbackUri("http://localhost:5173"))
                .isEqualTo("http://localhost:5173/oauth/callback");
        assertThat(properties.callbackUri("https://ssafyslap.vercel.app"))
                .isEqualTo("https://ssafyslap.vercel.app/oauth/callback");
    }

    @Test
    void fallsBackToDefaultCallbackUriForUnknownOrigin() {
        AppFrontendProperties properties = new AppFrontendProperties(
                "http://localhost:5173,https://ssafyslap.vercel.app",
                "https://ssafyslap.vercel.app"
        );

        assertThat(properties.callbackUri("https://unknown.example.com"))
                .isEqualTo("https://ssafyslap.vercel.app/oauth/callback");
    }
}
