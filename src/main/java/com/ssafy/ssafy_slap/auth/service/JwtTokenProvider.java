package com.ssafy.ssafy_slap.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long accessTokenValidityMillis;
    private final Clock clock;

    @Autowired
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-millis}") long accessTokenValidityMillis
    ) {
        this(secret, accessTokenValidityMillis, Clock.systemUTC());
    }

    JwtTokenProvider(String secret, long accessTokenValidityMillis, Clock clock) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenValidityMillis = accessTokenValidityMillis;
        this.clock = clock;
    }

    public String createAccessToken(Long userId, String role) {
        Instant now = clock.instant();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(userId));
        payload.put("role", role);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusMillis(accessTokenValidityMillis).getEpochSecond());

        String unsignedToken = encodeJson(header) + "." + encodeJson(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtPrincipal parse(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        long expiresAt = ((Number) payload.get("exp")).longValue();
        if (clock.instant().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Expired token");
        }
        return new JwtPrincipal(
                Long.valueOf((String) payload.get("sub")),
                (String) payload.get("role")
        );
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(value));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode JWT", e);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            return OBJECT_MAPPER.readValue(URL_DECODER.decode(value), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token payload", e);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
