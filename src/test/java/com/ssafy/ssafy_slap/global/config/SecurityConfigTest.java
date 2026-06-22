package com.ssafy.ssafy_slap.global.config;

import com.ssafy.ssafy_slap.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void allowsLocalAndLanViteOriginsForApiRequests() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthenticationFilter.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/places/filters");

        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOriginPatterns())
                .contains("http://localhost:5173", "http://127.0.0.1:5173", "http://*:5173");
        assertThat(configuration.getAllowedMethods()).contains("GET");
        assertThat(configuration.getAllowedHeaders()).contains("*");
    }

    @Test
    void requiresAuthenticationForTripApiRequests() throws Exception {
        String securityConfig = Files.readString(Path.of("src/main/java/com/ssafy/ssafy_slap/global/config/SecurityConfig.java"));

        assertThat(securityConfig).contains(".requestMatchers(\"/api/trips/**\").authenticated()");
        assertThat(securityConfig).contains(".requestMatchers(\"/api/chats/**\").authenticated()");
        assertThat(securityConfig).contains(".anyRequest().permitAll()");
    }
}
