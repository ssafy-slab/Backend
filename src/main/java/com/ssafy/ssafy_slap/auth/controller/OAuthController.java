package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.OAuthTicketRequest;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.service.OAuthLoginTicketStore;
import com.ssafy.ssafy_slap.auth.service.OAuthService;
import com.ssafy.ssafy_slap.global.config.AppFrontendProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private static final String REDIRECT_ORIGIN_COOKIE = "slap_oauth_redirect_origin";
    private final OAuthService oauthService;
    private final OAuthLoginTicketStore ticketStore;
    private final AppFrontendProperties frontendProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthController(OAuthService oauthService, OAuthLoginTicketStore ticketStore, AppFrontendProperties frontendProperties) {
        this.oauthService = oauthService;
        this.ticketStore = ticketStore;
        this.frontendProperties = frontendProperties;
    }

    @PostMapping("/{provider}/authorize")
    public ResponseEntity<Void> authorize(@PathVariable String provider, HttpServletRequest request) {
        OAuthProvider oauthProvider = OAuthProvider.fromPath(provider);
        String state = createState();
        String frontendOrigin = frontendProperties.redirectOrigin(request.getHeader(HttpHeaders.ORIGIN));
        ResponseCookie stateCookie = ResponseCookie.from(STATE_COOKIE, state)
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(5))
                .build();
        ResponseCookie redirectOriginCookie = ResponseCookie.from(REDIRECT_ORIGIN_COOKIE, frontendOrigin)
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(5))
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .header(HttpHeaders.SET_COOKIE, redirectOriginCookie.toString())
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
            return redirectToFrontend(request, "?error=oauth_failed");
        }

        Cookie cookie = WebUtils.getCookie(request, STATE_COOKIE);
        try {
            AuthResponse response = oauthService.login(
                    OAuthProvider.fromPath(provider),
                    code,
                    state,
                    cookie == null ? null : cookie.getValue()
            );
            return redirectToFrontend(request, "?ticket=" + encode(ticketStore.create(response)));
        } catch (RuntimeException exception) {
            log.warn("OAuth callback failed. provider={}, hasCode={}, hasState={}, hasCookie={}, exception={}",
                    provider,
                    true,
                    state != null && !state.isBlank(),
                    cookie != null,
                    exception.toString(),
                    exception
            );
            return redirectToFrontend(request, "?error=oauth_failed");
        }
    }

    @PostMapping("/token")
    public AuthResponse exchangeTicket(@Valid @RequestBody OAuthTicketRequest request) {
        return ticketStore.consume(request.ticket());
    }

    private ResponseEntity<Void> redirectToFrontend(HttpServletRequest request, String fragment) {
        ResponseCookie expiredState = ResponseCookie.from(STATE_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        ResponseCookie expiredRedirectOrigin = ResponseCookie.from(REDIRECT_ORIGIN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/oauth")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        Cookie redirectOriginCookie = WebUtils.getCookie(request, REDIRECT_ORIGIN_COOKIE);
        String redirectOrigin = redirectOriginCookie == null ? null : redirectOriginCookie.getValue();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, expiredState.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRedirectOrigin.toString())
                .location(URI.create(frontendProperties.callbackUri(redirectOrigin) + fragment))
                .build();
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
