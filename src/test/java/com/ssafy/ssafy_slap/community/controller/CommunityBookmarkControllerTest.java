package com.ssafy.ssafy_slap.community.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.community.service.CommunityImageStorageService;
import com.ssafy.ssafy_slap.community.service.CommunityService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CommunityBookmarkControllerTest {

    @Test
    void forwardsAuthenticatedUserToBookmarkAndRemoveBookmark() {
        CommunityService communityService = mock(CommunityService.class);
        CommunityController controller = new CommunityController(
                communityService,
                mock(CommunityImageStorageService.class)
        );
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );

        controller.bookmarkPost(1L, authentication);
        controller.removeBookmark(1L, authentication);

        verify(communityService).bookmarkPost(1L, 7L);
        verify(communityService).removeBookmark(1L, 7L);
    }
}
