package com.ssafy.ssafy_slap.user.service;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.dto.PasswordChangeRequest;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void updatesCurrentUserNickname() {
        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper, mock(PasswordEncoder.class));

        AppUser user = activeUser(1L, "old");
        AppUser updatedUser = activeUser(1L, "new");
        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(user), Optional.of(updatedUser));

        AuthUserResponse response = userService.updateProfile(1L, new ProfileUpdateRequest("  new  "));

        verify(userMapper).updateNickname(1L, "new");
        assertThat(response.nickname()).isEqualTo("new");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    void anonymizesUserAndUnlinksOAuthAccountsWhenDeletingCurrentUser() {
        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper, mock(PasswordEncoder.class));

        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(activeUser(1L, "tester")));

        userService.deleteAccount(1L);

        verify(userMapper).deleteOAuthAccounts(1L);
        verify(userMapper).anonymizeAndSoftDelete(1L, "deleted_1@deleted.slap.local");
        verify(userMapper, never()).softDelete(1L);
    }

    @Test
    void rejectsBlankNickname() {
        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper, mock(PasswordEncoder.class));

        assertThatThrownBy(() -> userService.updateProfile(1L, new ProfileUpdateRequest(" ")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nickname is required");
    }

    @Test
    void changesPasswordWhenCurrentPasswordMatches() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userMapper, passwordEncoder);
        AppUser user = activeUser(1L, "tester");

        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword1", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPassword2")).thenReturn("encoded-new");

        userService.changePassword(1L, new PasswordChangeRequest("oldPassword1", "newPassword2"));

        verify(userMapper).updatePasswordHash(1L, "encoded-new");
    }

    @Test
    void rejectsPasswordChangeWhenCurrentPasswordDoesNotMatch() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userMapper, passwordEncoder);
        AppUser user = activeUser(1L, "tester");

        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword1", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(
                1L,
                new PasswordChangeRequest("wrongPassword1", "newPassword2")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userMapper, never()).updatePasswordHash(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsPasswordChangeForOAuthAccount() {
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = new UserService(userMapper, passwordEncoder);
        AppUser user = activeUser(1L, "tester");
        user.setPasswordHash(null);

        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changePassword(
                1L,
                new PasswordChangeRequest("oldPassword1", "newPassword2")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("OAuth account");

        verify(userMapper, never()).updatePasswordHash(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }

    private AppUser activeUser(Long userId, String nickname) {
        return new AppUser(
                userId,
                "test@example.com",
                "encoded",
                nickname,
                "USER",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
