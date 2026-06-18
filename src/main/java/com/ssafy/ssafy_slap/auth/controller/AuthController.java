package com.ssafy.ssafy_slap.auth.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.LoginRequest;
import com.ssafy.ssafy_slap.auth.dto.SignupRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetEmailRequest;
import com.ssafy.ssafy_slap.auth.service.AuthService;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
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
}
