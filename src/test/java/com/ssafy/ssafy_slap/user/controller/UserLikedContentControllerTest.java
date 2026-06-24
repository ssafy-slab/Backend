package com.ssafy.ssafy_slap.user.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.community.service.CommunityService;
import com.ssafy.ssafy_slap.user.service.UserService;
import com.ssafy.ssafy_slap.place.service.PlaceService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserLikedContentControllerTest {

    @Test
    void forwardsAuthenticatedUserToLikedCommunityPostLookup() {
        CommunityService communityService = mock(CommunityService.class);
        PlaceService placeService = mock(PlaceService.class);
        UserController controller = new UserController(mock(UserService.class), communityService, placeService);
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );

        controller.getLikedCommunityPosts(authentication, 1, 10);
        controller.getLikedPlaces(authentication, 2, 15);

        verify(communityService).findLikedPosts(7L, 1, 10);
        verify(placeService).findLikedPlaces(7L, 2, 15);
    }
}
