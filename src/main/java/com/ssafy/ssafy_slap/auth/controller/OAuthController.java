package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.service.OAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);
    private static final String STATE_COOKIE = "slap_oauth_state";
    private final OAuthService oauthService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/{provider}/authorize")
    public ResponseEntity<Void> authorize(@PathVariable String provider) {
        OAuthProvider oauthProvider = OAuthProvider.fromPath(provider);
        String state = createState();
        ResponseCookie stateCookie = ResponseCookie.from(STATE_COOKIE, state)
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(5))
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .location(URI.create(oauthService.createAuthorizationUrl(oauthProvider, state)))
                .build();
    }

    @GetMapping("/{provider}/callback")
    public ResponseEntity<Void> callback(
            @PathVariable String provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request
    ) {
        if (error != null || code == null || code.isBlank()) {
            log.warn("OAuth callback rejected before token exchange. provider={}, error={}, hasCode={}",
                    provider,
                    error,
                    code != null && !code.isBlank()
            );
            return redirectToFrontend("#error=oauth_failed");
        }

        Cookie cookie = WebUtils.getCookie(request, STATE_COOKIE);
        try {
            AuthResponse response = oauthService.login(
                    OAuthProvider.fromPath(provider),
                    code,
                    state,
                    cookie == null ? null : cookie.getValue()
            );
            return redirectToFrontend(successFragment(response));
        } catch (RuntimeException exception) {
            log.warn("OAuth callback failed. provider={}, hasCode={}, hasState={}, hasCookie={}, exception={}",
                    provider,
                    true,
                    state != null && !state.isBlank(),
                    cookie != null,
                    exception.toString(),
                    exception
            );
            return redirectToFrontend("#error=oauth_failed");
        }
    }

    private ResponseEntity<Void> redirectToFrontend(String fragment) {
        ResponseCookie expiredState = ResponseCookie.from(STATE_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, expiredState.toString())
                .location(URI.create(oauthService.frontendRedirectUri() + fragment))
                .build();
    }

    private String successFragment(AuthResponse response) {
        return "#tokenType=" + encode(response.tokenType())
                + "&accessToken=" + encode(response.accessToken())
                + "&userId=" + response.user().userId()
                + "&email=" + encode(response.user().email())
                + "&nickname=" + encode(response.user().nickname())
                + "&role=" + encode(response.user().role());
    }

    private String encode(String value) {
        return UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }

    private String createState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
