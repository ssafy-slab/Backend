package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.user.domain.AppUser;

public record RefreshRotation(AppUser user, IssuedRefreshToken refreshToken) {
}
