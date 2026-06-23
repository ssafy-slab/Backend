package com.ssafy.ssafy_slap.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AppFrontendProperties {

    public static final String DEFAULT_FRONTEND_ORIGINS =
            "http://localhost:5173,http://127.0.0.1:5173,http://*:5173,https://ssafyslap.vercel.app";
    public static final String DEFAULT_FRONTEND_ORIGIN = "https://ssafyslap.vercel.app";

    private final List<String> allowedOriginPatterns;
    private final String defaultOrigin;

    public AppFrontendProperties(
            @Value("${app.frontend-origins:" + DEFAULT_FRONTEND_ORIGINS + "}") String frontendOrigins,
            @Value("${app.default-frontend-origin:" + DEFAULT_FRONTEND_ORIGIN + "}") String defaultOrigin
    ) {
        this.allowedOriginPatterns = parseOrigins(frontendOrigins);
        this.defaultOrigin = defaultOrigin.trim();
    }

    public List<String> allowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public String redirectOrigin(String origin) {
        return allowedRedirectOriginOrDefault(origin);
    }

    public String callbackUri(String origin) {
        return allowedRedirectOriginOrDefault(origin) + "/oauth/callback";
    }

    private String allowedRedirectOriginOrDefault(String origin) {
        if (origin != null) {
            String trimmedOrigin = origin.trim();
            if (allowedOriginPatterns.contains(trimmedOrigin)) {
                return trimmedOrigin;
            }
        }
        return defaultOrigin;
    }

    private List<String> parseOrigins(String frontendOrigins) {
        return Arrays.stream(frontendOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();
    }
}
