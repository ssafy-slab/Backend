package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.dto.LoginRequest;
import com.ssafy.ssafy_slap.auth.dto.SignupRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetRequest;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionService authSessionService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider legacyTokenProvider;

    @Autowired
    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            AuthSessionService authSessionService,
            RefreshTokenService refreshTokenService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authSessionService = authSessionService;
        this.refreshTokenService = refreshTokenService;
        this.legacyTokenProvider = null;
    }

    AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authSessionService = null;
        this.refreshTokenService = null;
        this.legacyTokenProvider = tokenProvider;
    }

    @Transactional
    public AuthSession signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userMapper.existsActiveByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        String nickname = request.nickname().trim();
        AppUser deletedUser = userMapper.findByEmail(email)
                .filter(candidate -> "DELETED".equals(candidate.getStatus()))
                .orElse(null);
        if (deletedUser != null) {
            userMapper.anonymizeDeletedUserEmail(deletedUser.getUserId(), deletedEmail(deletedUser.getUserId()));
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setNickname(nickname);
        user.setRole("USER");
        user.setStatus("ACTIVE");

        userMapper.insertLocalUser(user);
        return createAuthResponse(user);
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        return login(request.email(), request.password());
    }

    @Transactional
    public AuthSession login(String email, String password) {
        AppUser user = userMapper.findActiveByEmail(normalizeEmail(email))
                .filter(candidate -> candidate.getPasswordHash() != null)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return createAuthResponse(user);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        AppUser user = findActiveLocalUser(request.email());
        userMapper.updatePasswordHash(user.getUserId(), passwordEncoder.encode(request.newPassword()));
        if (refreshTokenService != null) {
            refreshTokenService.revokeAll(user.getUserId());
        }
    }

    @Transactional(readOnly = true)
    public void verifyPasswordResetEmail(String email) {
        findActiveLocalUser(email);
    }

    private AppUser findActiveLocalUser(String email) {
        AppUser user = userMapper.findActiveByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Local account not found"));
        if (user.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth account cannot reset password");
        }
        return user;
    }

    private AuthSession createAuthResponse(AppUser user) {
        if (authSessionService != null) {
            return authSessionService.create(user);
        }
        return new AuthSession(
                new com.ssafy.ssafy_slap.auth.dto.AuthResponse(
                        "Bearer",
                        legacyTokenProvider.createAccessToken(user.getUserId(), user.getRole()),
                        AuthUserResponse.from(user)
                ),
                new IssuedRefreshToken("test-refresh-token", java.time.Instant.MAX)
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String deletedEmail(Long userId) {
        return "deleted_" + userId + "@deleted.slap.local";
    }
}
