package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.dto.LoginRequest;
import com.ssafy.ssafy_slap.auth.dto.SignupRequest;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
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

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        return login(request.email(), request.password());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        AppUser user = userMapper.findActiveByEmail(normalizeEmail(email))
                .filter(candidate -> candidate.getPasswordHash() != null)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(AppUser user) {
        return new AuthResponse(
                "Bearer",
                tokenProvider.createAccessToken(user.getUserId(), user.getRole()),
                AuthUserResponse.from(user)
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String deletedEmail(Long userId) {
        return "deleted_" + userId + "@deleted.slap.local";
    }
}
