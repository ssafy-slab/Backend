package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.oauth.OAuthClient;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProperties;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProviderProfile;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;

@Service
public class OAuthService {

    private final UserMapper userMapper;
    private final OAuthClient oauthClient;
    private final JwtTokenProvider tokenProvider;
    private final OAuthProperties properties;
    private final SecureRandom secureRandom;

    public OAuthService(UserMapper userMapper, OAuthClient oauthClient, JwtTokenProvider tokenProvider) {
        this(userMapper, oauthClient, tokenProvider, new OAuthProperties(), new SecureRandom());
    }

    @Autowired
    public OAuthService(UserMapper userMapper, OAuthClient oauthClient, JwtTokenProvider tokenProvider, OAuthProperties properties) {
        this(userMapper, oauthClient, tokenProvider, properties, new SecureRandom());
    }

    OAuthService(
            UserMapper userMapper,
            OAuthClient oauthClient,
            JwtTokenProvider tokenProvider,
            OAuthProperties properties,
            SecureRandom secureRandom
    ) {
        this.userMapper = userMapper;
        this.oauthClient = oauthClient;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
        this.secureRandom = secureRandom;
    }

    public String createAuthorizationUrl(OAuthProvider provider, String state) {
        OAuthProperties.Provider providerProperties = properties.provider(provider);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(provider.authorizationUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", providerProperties.getClientId())
                .queryParam("redirect_uri", properties.redirectUri(provider))
                .queryParam("state", state);

        if (provider.scope() != null && !provider.scope().isBlank()) {
            builder.queryParam("scope", provider.scope());
        }

        return builder.encode().build().toUriString();
    }

    @Transactional
    public AuthResponse login(OAuthProvider provider, String code, String state, String expectedState) {
        if (state == null || expectedState == null || !state.equals(expectedState)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OAuth state");
        }

        var token = oauthClient.exchangeCode(provider, code, state);
        OAuthProviderProfile profile = oauthClient.fetchProfile(provider, token.accessToken());
        if (profile.providerUserId() == null || profile.providerUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OAuth profile did not include provider user id");
        }

        String providerName = provider.name();
        AppUser user = userMapper.findActiveByOAuthAccount(providerName, profile.providerUserId())
                .orElseGet(() -> restoreOrCreateUser(providerName, profile));

        return createAuthResponse(user);
    }

    public String frontendRedirectUri() {
        return properties.getFrontendRedirectUri();
    }

    private AppUser restoreOrCreateUser(String provider, OAuthProviderProfile profile) {
        AppUser deletedOAuthUser = userMapper.findByOAuthAccount(provider, profile.providerUserId())
                .filter(candidate -> "DELETED".equals(candidate.getStatus()))
                .orElse(null);
        if (deletedOAuthUser != null) {
            userMapper.deleteOAuthAccounts(deletedOAuthUser.getUserId());
            userMapper.anonymizeDeletedUserEmail(deletedOAuthUser.getUserId(), deletedEmail(deletedOAuthUser.getUserId()));
        }

        String email = normalizeEmail(profile.email());
        AppUser user = email.isBlank()
                ? createOAuthUser(availableEmail(syntheticEmail(provider, profile.providerUserId())), uniqueProviderNickname(provider))
                : userMapper.findActiveByEmail(email)
                        .orElseGet(() -> createOAuthUser(availableEmail(email), uniqueProviderNickname(provider)));

        userMapper.insertOAuthAccount(user.getUserId(), provider, profile.providerUserId(), email.isBlank() ? null : email);
        return user;
    }

    private AppUser createOAuthUser(String email, String nickname) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(null);
        user.setNickname(normalizeNickname(nickname));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insertLocalUser(user);
        return user;
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

    private String normalizeNickname(String nickname) {
        return nickname == null || nickname.isBlank() ? "SLAP 사용자" : nickname.trim();
    }

    private String providerNickname(String provider) {
        return switch (provider) {
            case "KAKAO" -> "카카오여행자";
            case "NAVER" -> "네이버여행자";
            case "GOOGLE" -> "구글여행자";
            default -> "SLAP여행자";
        };
    }

    private String uniqueProviderNickname(String provider) {
        String baseNickname = providerNickname(provider);
        for (int attempt = 0; attempt < 100; attempt++) {
            String nickname = baseNickname + String.format("%04d", secureRandom.nextInt(10_000));
            if (!userMapper.existsActiveByNickname(nickname)) {
                return nickname;
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not create unique OAuth nickname");
    }

    private String syntheticEmail(String provider, String providerUserId) {
        return provider.toLowerCase() + "_" + providerUserId + "@oauth.slap.local";
    }

    private String availableEmail(String email) {
        userMapper.findByEmail(email)
                .filter(candidate -> "DELETED".equals(candidate.getStatus()))
                .ifPresent(candidate -> userMapper.anonymizeDeletedUserEmail(
                        candidate.getUserId(),
                        deletedEmail(candidate.getUserId())
                ));
        return email;
    }

    private String deletedEmail(Long userId) {
        return "deleted_" + userId + "@deleted.slap.local";
    }
}
