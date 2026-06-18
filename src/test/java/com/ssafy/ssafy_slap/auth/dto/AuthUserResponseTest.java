package com.ssafy.ssafy_slap.auth.dto;

import com.ssafy.ssafy_slap.user.domain.AppUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserResponseTest {

    @Test
    void identifiesLocalAndOAuthAccountsFromPasswordHash() {
        AppUser localUser = user("encoded");
        AppUser oauthUser = user(null);

        assertThat(AuthUserResponse.from(localUser).localAccount()).isTrue();
        assertThat(AuthUserResponse.from(oauthUser).localAccount()).isFalse();
    }

    private AppUser user(String passwordHash) {
        return new AppUser(
                1L, "test@example.com", passwordHash, "tester", "USER", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
