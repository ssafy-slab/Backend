package com.ssafy.ssafy_slap.user.service;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.dto.PasswordChangeRequest;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthUserResponse findCurrentUser(Long userId) {
        return AuthUserResponse.from(findActiveUser(userId));
    }

    @Transactional
    public AuthUserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        String nickname = request.nickname() == null ? "" : request.nickname().trim();
        if (nickname.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname is required");
        }

        findActiveUser(userId);
        userMapper.updateNickname(userId, nickname);
        return AuthUserResponse.from(findActiveUser(userId));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        findActiveUser(userId);
        userMapper.deleteOAuthAccounts(userId);
        userMapper.anonymizeAndSoftDelete(userId, deletedEmail(userId));
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        AppUser user = findActiveUser(userId);
        if (user.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth account cannot change password");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        userMapper.updatePasswordHash(userId, passwordEncoder.encode(request.newPassword()));
    }

    private AppUser findActiveUser(Long userId) {
        return userMapper.findActiveById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String deletedEmail(Long userId) {
        return "deleted_" + userId + "@deleted.slap.local";
    }
}
