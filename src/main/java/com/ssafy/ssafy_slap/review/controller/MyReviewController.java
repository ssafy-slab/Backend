package com.ssafy.ssafy_slap.review.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.review.dto.MyPlaceReviewPageResponse;
import com.ssafy.ssafy_slap.review.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me/reviews")
public class MyReviewController {

    private final ReviewService reviewService;

    public MyReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public MyPlaceReviewPageResponse getMyReviews(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return reviewService.getMyReviews(user.userId(), page, size);
    }
}
