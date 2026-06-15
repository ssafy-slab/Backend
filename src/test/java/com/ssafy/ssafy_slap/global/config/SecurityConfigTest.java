package com.ssafy.ssafy_slap.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void allowsLocalViteOriginForApiRequests() {
        SecurityConfig securityConfig = new SecurityConfig();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/places/filters");

        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(configuration.getAllowedMethods()).contains("GET");
        assertThat(configuration.getAllowedHeaders()).contains("*");
    }
}
