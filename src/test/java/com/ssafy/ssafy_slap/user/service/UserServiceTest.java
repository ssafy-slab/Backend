package com.ssafy.ssafy_slap.user.service;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.user.domain.AppUser;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
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
        UserService userService = new UserService(userMapper);

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
        UserService userService = new UserService(userMapper);

        when(userMapper.findActiveById(1L)).thenReturn(Optional.of(activeUser(1L, "tester")));

        userService.deleteAccount(1L);

        verify(userMapper).deleteOAuthAccounts(1L);
        verify(userMapper).anonymizeAndSoftDelete(1L, "deleted_1@deleted.slap.local");
        verify(userMapper, never()).softDelete(1L);
    }

    @Test
    void rejectsBlankNickname() {
        UserMapper userMapper = mock(UserMapper.class);
        UserService userService = new UserService(userMapper);

        assertThatThrownBy(() -> userService.updateProfile(1L, new ProfileUpdateRequest(" ")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nickname is required");
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
