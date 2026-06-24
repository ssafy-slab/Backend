package com.ssafy.ssafy_slap.user.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.community.dto.CommunityPostSummaryResponse;
import com.ssafy.ssafy_slap.community.service.CommunityService;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.dto.PasswordChangeRequest;
import com.ssafy.ssafy_slap.user.service.UserService;
import com.ssafy.ssafy_slap.place.service.PlaceService;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final CommunityService communityService;
    private final PlaceService placeService;

    public UserController(UserService userService, CommunityService communityService, PlaceService placeService) {
        this.userService = userService;
        this.communityService = communityService;
        this.placeService = placeService;
    }

    @GetMapping
    public AuthUserResponse getCurrentUser(Authentication authentication) {
        return userService.findCurrentUser(currentUserId(authentication));
    }

    @GetMapping("/liked-community-posts")
    public java.util.List<CommunityPostSummaryResponse> getLikedCommunityPosts(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return communityService.findLikedPosts(currentUserId(authentication), page, size);
    }

    @GetMapping("/liked-places")
    public java.util.List<PlaceSummaryResponse> getLikedPlaces(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return placeService.findLikedPlaces(currentUserId(authentication), page, size);
    }

    @PatchMapping
    public AuthUserResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return userService.updateProfile(currentUserId(authentication), request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication authentication) {
        userService.deleteAccount(currentUserId(authentication));
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(currentUserId(authentication), request);
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
