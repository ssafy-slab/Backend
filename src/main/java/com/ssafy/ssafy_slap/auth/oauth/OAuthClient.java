package com.ssafy.ssafy_slap.auth.oauth;

public interface OAuthClient {

    OAuthTokenResponse exchangeCode(OAuthProvider provider, String code, String state);

    OAuthProviderProfile fetchProfile(OAuthProvider provider, String accessToken);
}
