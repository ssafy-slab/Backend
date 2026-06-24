package com.ssafy.ssafy_slap.user.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.community.service.CommunityService;
import com.ssafy.ssafy_slap.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserBookmarkControllerTest {

    @Test
    void forwardsAuthenticatedUserToBookmarkedCommunityPostLookup() {
        CommunityService communityService = mock(CommunityService.class);
        UserController controller = new UserController(mock(UserService.class), communityService);
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );

        controller.getBookmarkedCommunityPosts(authentication, 1, 10);

        verify(communityService).findBookmarkedPosts(7L, 1, 10);
    }
}
