package com.ssafy.ssafy_slap.auth.dto;

import com.ssafy.ssafy_slap.user.domain.AppUser;

public record AuthUserResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {

    public static AuthUserResponse from(AppUser user) {
        return new AuthUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}
