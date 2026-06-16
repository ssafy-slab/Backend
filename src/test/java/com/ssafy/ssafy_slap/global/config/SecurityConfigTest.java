package com.ssafy.ssafy_slap.global.config;

import com.ssafy.ssafy_slap.global.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void allowsLocalViteOriginForApiRequests() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JwtAuthenticationFilter.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/places/filters");

        CorsConfiguration configuration = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(configuration.getAllowedMethods()).contains("GET");
        assertThat(configuration.getAllowedHeaders()).contains("*");
    }
}
