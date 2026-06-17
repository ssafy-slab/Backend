package com.ssafy.ssafy_slap.auth.oauth;

import java.util.Arrays;

public enum OAuthProvider {
    KAKAO(
            "kakao",
            "https://kauth.kakao.com/oauth/authorize",
            "https://kauth.kakao.com/oauth/token",
            "https://kapi.kakao.com/v2/user/me",
            null
    ),
    GOOGLE(
            "google",
            "https://accounts.google.com/o/oauth2/v2/auth",
            "https://oauth2.googleapis.com/token",
            "https://openidconnect.googleapis.com/v1/userinfo",
            "openid email profile"
    ),
    NAVER(
            "naver",
            "https://nid.naver.com/oauth2.0/authorize",
            "https://nid.naver.com/oauth2.0/token",
            "https://openapi.naver.com/v1/nid/me",
            null
    );

    private final String path;
    private final String authorizationUrl;
    private final String tokenUrl;
    private final String profileUrl;
    private final String scope;

    OAuthProvider(String path, String authorizationUrl, String tokenUrl, String profileUrl, String scope) {
        this.path = path;
        this.authorizationUrl = authorizationUrl;
        this.tokenUrl = tokenUrl;
        this.profileUrl = profileUrl;
        this.scope = scope;
    }

    public static OAuthProvider fromPath(String path) {
        return Arrays.stream(values())
                .filter(provider -> provider.path.equalsIgnoreCase(path))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth provider: " + path));
    }

    public String path() {
        return path;
    }

    public String authorizationUrl() {
        return authorizationUrl;
    }

    public String tokenUrl() {
        return tokenUrl;
    }

    public String profileUrl() {
        return profileUrl;
    }

    public String scope() {
        return scope;
    }
}
