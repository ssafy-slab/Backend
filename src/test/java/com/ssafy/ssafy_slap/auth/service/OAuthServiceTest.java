package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.oauth.OAuthClient;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProvider;
import com.ssafy.ssafy_slap.auth.oauth.OAuthProviderProfile;
import com.ssafy.ssafy_slap.auth.oauth.OAuthTokenResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.security.SecureRandom;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthServiceTest {

    @ParameterizedTest
    @CsvSource({
            "KAKAO, 카카오여행자",
            "NAVER, 네이버여행자",
            "GOOGLE, 구글여행자"
    })
    void createsNewOAuthUserWithProviderTravelerNickname(
            OAuthProvider provider,
            String expectedNickname
    ) {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        SecureRandom secureRandom = mock(SecureRandom.class);
        OAuthService service = new OAuthService(
                userMapper,
                oauthClient,
                tokenProvider,
                new com.ssafy.ssafy_slap.auth.oauth.OAuthProperties(),
                secureRandom
        );
        String providerName = provider.name();
        String providerUserId = provider.name().toLowerCase() + "-user-1";
        String email = provider.name().toLowerCase() + "@example.com";

        when(userMapper.findActiveByOAuthAccount(providerName, providerUserId)).thenReturn(Optional.empty());
        when(userMapper.findByOAuthAccount(providerName, providerUserId)).thenReturn(Optional.empty());
        when(userMapper.findActiveByEmail(email)).thenReturn(Optional.empty());
        when(userMapper.findByEmail(email)).thenReturn(Optional.empty());
        when(secureRandom.nextInt(10_000)).thenReturn(42);
        when(userMapper.existsActiveByNickname(expectedNickname + "0042")).thenReturn(false);
        when(oauthClient.exchangeCode(provider, "code-123", "state-123"))
                .thenReturn(new OAuthTokenResponse("provider-access-token"));
        when(oauthClient.fetchProfile(provider, "provider-access-token"))
                .thenReturn(new OAuthProviderProfile(providerUserId, email, "실명 사용자"));
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(77L);
            return null;
        }).when(userMapper).insertLocalUser(any(AppUser.class));
        when(tokenProvider.createAccessToken(77L, "USER")).thenReturn("access-token");

        service.login(provider, "code-123", "state-123", "state-123");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userMapper).insertLocalUser(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo(expectedNickname + "0042");
    }

    @Test
    void retriesWhenGeneratedOAuthNicknameAlreadyExists() {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        SecureRandom secureRandom = mock(SecureRandom.class);
        OAuthService service = new OAuthService(
                userMapper,
                oauthClient,
                tokenProvider,
                new com.ssafy.ssafy_slap.auth.oauth.OAuthProperties(),
                secureRandom
        );

        when(userMapper.findActiveByOAuthAccount("KAKAO", "kakao-user-1")).thenReturn(Optional.empty());
        when(userMapper.findByOAuthAccount("KAKAO", "kakao-user-1")).thenReturn(Optional.empty());
        when(userMapper.findActiveByEmail("kakao@example.com")).thenReturn(Optional.empty());
        when(userMapper.findByEmail("kakao@example.com")).thenReturn(Optional.empty());
        when(oauthClient.exchangeCode(OAuthProvider.KAKAO, "code-123", "state-123"))
                .thenReturn(new OAuthTokenResponse("provider-access-token"));
        when(oauthClient.fetchProfile(OAuthProvider.KAKAO, "provider-access-token"))
                .thenReturn(new OAuthProviderProfile("kakao-user-1", "kakao@example.com", "실명 사용자"));
        when(secureRandom.nextInt(10_000)).thenReturn(42, 731);
        when(userMapper.existsActiveByNickname("카카오여행자0042")).thenReturn(true);
        when(userMapper.existsActiveByNickname("카카오여행자0731")).thenReturn(false);
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(77L);
            return null;
        }).when(userMapper).insertLocalUser(any(AppUser.class));
        when(tokenProvider.createAccessToken(77L, "USER")).thenReturn("access-token");

        service.login(OAuthProvider.KAKAO, "code-123", "state-123", "state-123");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userMapper).insertLocalUser(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("카카오여행자0731");
    }

    @Test
    void createsAuthorizationUrlWithStateAndProviderConfig() {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        OAuthService service = new OAuthService(userMapper, oauthClient, tokenProvider);

        String url = service.createAuthorizationUrl(OAuthProvider.KAKAO, "state-123");

        assertThat(url).startsWith("https://kauth.kakao.com/oauth/authorize?");
        assertThat(url).contains("response_type=code");
        assertThat(url).contains("client_id=");
        assertThat(url).contains("redirect_uri=");
        assertThat(url).contains("state=state-123");
    }

    @Test
    void encodesAuthorizationUrlQueryParameters() {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        OAuthService service = new OAuthService(userMapper, oauthClient, tokenProvider);

        String url = service.createAuthorizationUrl(OAuthProvider.GOOGLE, "state-123");

        assertThat(url).doesNotContain(" ");
        assertThat(url).contains("scope=openid%20email%20profile");
    }

    @Test
    void logsInByCreatingUserAndOAuthAccountWhenProviderAccountIsNew() {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        OAuthService service = new OAuthService(userMapper, oauthClient, tokenProvider);

        when(userMapper.findActiveByOAuthAccount("GOOGLE", "google-user-1")).thenReturn(Optional.empty());
        when(oauthClient.exchangeCode(OAuthProvider.GOOGLE, "code-123", "state-123"))
                .thenReturn(new OAuthTokenResponse("provider-access-token"));
        when(oauthClient.fetchProfile(OAuthProvider.GOOGLE, "provider-access-token"))
                .thenReturn(new OAuthProviderProfile("google-user-1", "USER@example.com", "Google User"));
        when(userMapper.findActiveByEmail("user@example.com")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(77L);
            return null;
        }).when(userMapper).insertLocalUser(any(AppUser.class));
        when(tokenProvider.createAccessToken(77L, "USER")).thenReturn("jwt-token");

        var response = service.login(OAuthProvider.GOOGLE, "code-123", "state-123", "state-123");

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().nickname()).startsWith("구글여행자");
        assertThat(response.user().nickname()).matches("구글여행자\\d{4}");
        verify(userMapper).insertOAuthAccount(77L, "GOOGLE", "google-user-1", "user@example.com");
    }

    @Test
    void createsNewOAuthUserAfterDeletedOAuthAccountIsUnlinked() {
        UserMapper userMapper = mock(UserMapper.class);
        OAuthClient oauthClient = mock(OAuthClient.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        OAuthService service = new OAuthService(userMapper, oauthClient, tokenProvider);

        AppUser deletedUser = new AppUser(
                77L,
                "kakao-77@oauth.slap.local",
                null,
                "old",
                "USER",
                "DELETED",
                null,
                null
        );
        when(userMapper.findActiveByOAuthAccount("KAKAO", "kakao-user-1")).thenReturn(Optional.empty());
        when(userMapper.findByOAuthAccount("KAKAO", "kakao-user-1")).thenReturn(Optional.of(deletedUser));
        when(oauthClient.exchangeCode(OAuthProvider.KAKAO, "code-123", "state-123"))
                .thenReturn(new OAuthTokenResponse("provider-access-token"));
        when(oauthClient.fetchProfile(OAuthProvider.KAKAO, "provider-access-token"))
                .thenReturn(new OAuthProviderProfile("kakao-user-1", "", "Kakao User"));
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(88L);
            return null;
        }).when(userMapper).insertLocalUser(any(AppUser.class));
        when(tokenProvider.createAccessToken(88L, "USER")).thenReturn("jwt-token");

        var response = service.login(OAuthProvider.KAKAO, "code-123", "state-123", "state-123");

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("kakao_kakao-user-1@oauth.slap.local");
        verify(userMapper).deleteOAuthAccounts(77L);
        verify(userMapper).anonymizeDeletedUserEmail(77L, "deleted_77@deleted.slap.local");
        verify(userMapper).insertOAuthAccount(88L, "KAKAO", "kakao-user-1", null);
    }
}
