package com.ssafy.ssafy_slap.auth.service;

import java.security.Principal;

public record AuthenticatedUser(
        Long userId,
        String role
) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
