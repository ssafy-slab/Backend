package com.ssafy.ssafy_slap.auth.service;

import java.time.Instant;

public record IssuedRefreshToken(String token, Instant expiresAt) {
}
