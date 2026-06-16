package com.ssafy.ssafy_slap.user.service;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        userMapper.softDelete(userId);
    }

    private AppUser findActiveUser(Long userId) {
        return userMapper.findActiveById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
