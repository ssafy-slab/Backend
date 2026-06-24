package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.LoginRequest;
import com.ssafy.ssafy_slap.auth.dto.SignupRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetEmailRequest;
import com.ssafy.ssafy_slap.auth.service.AuthService;
import com.ssafy.ssafy_slap.auth.service.AuthSession;
import com.ssafy.ssafy_slap.auth.service.AuthSessionService;
import com.ssafy.ssafy_slap.auth.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthController(
            AuthService authService,
            AuthSessionService authSessionService,
            RefreshTokenService refreshTokenService,
            RefreshTokenCookie refreshTokenCookie
    ) {
        this.authService = authService;
        this.authSessionService = authSessionService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return withRefreshCookie(authService.signup(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return withRefreshCookie(authService.login(request), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("X-Refresh-Request") String refreshRequest,
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken
    ) {
        requireRefreshRequestHeader(refreshRequest);
        return withRefreshCookie(authSessionService.refresh(refreshToken), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("X-Refresh-Request") String refreshRequest,
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken
    ) {
        requireRefreshRequestHeader(refreshRequest);
        refreshTokenService.revoke(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
                .build();
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
    }

    @PostMapping("/password/reset/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyPasswordResetEmail(@Valid @RequestBody PasswordResetEmailRequest request) {
        authService.verifyPasswordResetEmail(request.email());
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(AuthSession session, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(
                        session.refreshToken().token(),
                        session.refreshToken().expiresAt()
                ).toString())
                .body(session.response());
    }

    private void requireRefreshRequestHeader(String value) {
        if (!"true".equalsIgnoreCase(value)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Refresh request header is required"
            );
        }
    }
}
