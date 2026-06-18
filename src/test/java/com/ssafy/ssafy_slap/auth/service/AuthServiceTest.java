package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.SignupRequest;
import com.ssafy.ssafy_slap.auth.dto.PasswordResetRequest;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void signsUpLocalUserWithEncodedPasswordAndToken() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);

        when(userMapper.existsActiveByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123!")).thenReturn("encoded-password");
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(10L);
            return null;
        }).when(userMapper).insertLocalUser(org.mockito.ArgumentMatchers.any(AppUser.class));
        when(tokenProvider.createAccessToken(10L, "USER")).thenReturn("access-token");

        var request = new SignupRequest("test@example.com", "password123!", "여행자");
        var response = authService.signup(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userMapper).insertLocalUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getNickname()).isEqualTo("여행자");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
    }

    @Test
    void rejectsLoginWhenPasswordDoesNotMatch() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);

        when(userMapper.findActiveByEmail("test@example.com")).thenReturn(Optional.of(new AppUser(
                10L,
                "test@example.com",
                "encoded-password",
                "여행자",
                "USER",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        )));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("test@example.com", "wrong-password"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void createsNewLocalUserAfterDeletedEmailIsAnonymized() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);

        AppUser deletedUser = new AppUser(
                10L,
                "test@example.com",
                "old-password",
                "old",
                "USER",
                "DELETED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userMapper.existsActiveByEmail("test@example.com")).thenReturn(false);
        when(userMapper.findByEmail("test@example.com")).thenReturn(Optional.of(deletedUser));
        when(passwordEncoder.encode("password123!")).thenReturn("new-password");
        doAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setUserId(11L);
            return null;
        }).when(userMapper).insertLocalUser(org.mockito.ArgumentMatchers.any(AppUser.class));
        when(tokenProvider.createAccessToken(11L, "USER")).thenReturn("access-token");

        var response = authService.signup(new SignupRequest("test@example.com", "password123!", "tester"));

        verify(userMapper).anonymizeDeletedUserEmail(10L, "deleted_10@deleted.slap.local");
        verify(userMapper).insertLocalUser(org.mockito.ArgumentMatchers.any(AppUser.class));
        assertThat(response.user().nickname()).isEqualTo("tester");
        assertThat(response.user().email()).isEqualTo("test@example.com");
        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void resetsPasswordForActiveLocalAccount() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);
        AppUser user = new AppUser(
                10L, "test@example.com", "encoded-old", "tester", "USER", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userMapper.findActiveByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword2")).thenReturn("encoded-new");

        authService.resetPassword(new PasswordResetRequest(" TEST@example.com ", "newPassword2"));

        verify(userMapper).updatePasswordHash(10L, "encoded-new");
    }

    @Test
    void rejectsPasswordResetForUnknownEmail() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);

        when(userMapper.findActiveByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                new PasswordResetRequest("missing@example.com", "newPassword2")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Local account not found");

        verify(userMapper, never()).updatePasswordHash(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsPasswordResetForOAuthAccount() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);
        AppUser user = new AppUser(
                10L, "oauth@example.com", null, "tester", "USER", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userMapper.findActiveByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(
                new PasswordResetRequest("oauth@example.com", "newPassword2")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("OAuth account");

        verify(userMapper, never()).updatePasswordHash(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void verifiesActiveLocalAccountBeforePasswordReset() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);
        AppUser user = new AppUser(
                10L, "test@example.com", "encoded-password", "tester", "USER", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userMapper.findActiveByEmail("test@example.com")).thenReturn(Optional.of(user));

        authService.verifyPasswordResetEmail(" TEST@example.com ");

        verify(userMapper).findActiveByEmail("test@example.com");
    }

    @Test
    void rejectsUnknownEmailBeforePasswordReset() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);

        when(userMapper.findActiveByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyPasswordResetEmail("missing@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Local account not found");
    }

    @Test
    void rejectsOAuthEmailBeforePasswordReset() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        AuthService authService = new AuthService(userMapper, passwordEncoder, tokenProvider);
        AppUser user = new AppUser(
                10L, "oauth@example.com", null, "tester", "USER", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(userMapper.findActiveByEmail("oauth@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyPasswordResetEmail("oauth@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("OAuth account");
    }
}
