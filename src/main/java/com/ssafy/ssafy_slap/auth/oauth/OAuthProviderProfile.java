package com.ssafy.ssafy_slap.auth.oauth;

public record OAuthProviderProfile(
        String providerUserId,
        String email,
        String nickname
) {
}
