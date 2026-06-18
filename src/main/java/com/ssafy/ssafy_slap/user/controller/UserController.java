package com.ssafy.ssafy_slap.user.controller;

import com.ssafy.ssafy_slap.auth.dto.AuthUserResponse;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.user.dto.ProfileUpdateRequest;
import com.ssafy.ssafy_slap.user.dto.PasswordChangeRequest;
import com.ssafy.ssafy_slap.user.service.UserService;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public AuthUserResponse getCurrentUser(Authentication authentication) {
        return userService.findCurrentUser(currentUserId(authentication));
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
