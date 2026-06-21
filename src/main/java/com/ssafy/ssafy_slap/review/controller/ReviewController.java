package com.ssafy.ssafy_slap.review.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewRequest;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewSummaryResponse;
import com.ssafy.ssafy_slap.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public PlaceReviewSummaryResponse getReviews(
            @PathVariable Long placeId,
            Authentication authentication
    ) {
        return reviewService.getReviews(placeId, optionalCurrentUserId(authentication));
    }

    @PostMapping
    public PlaceReviewSummaryResponse create(
            @PathVariable Long placeId,
            Authentication authentication,
            @Valid @RequestBody PlaceReviewRequest request
    ) {
        return reviewService.create(placeId, currentUserId(authentication), request);
    }

    @PutMapping("/me")
    public PlaceReviewSummaryResponse update(
            @PathVariable Long placeId,
            Authentication authentication,
            @Valid @RequestBody PlaceReviewRequest request
    ) {
        return reviewService.update(placeId, currentUserId(authentication), request);
    }

    @DeleteMapping("/me")
    public PlaceReviewSummaryResponse delete(
            @PathVariable Long placeId,
            Authentication authentication
    ) {
        return reviewService.delete(placeId, currentUserId(authentication));
    }

    private Long optionalCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId();
        }
        return null;
    }

    private Long currentUserId(Authentication authentication) {
        Long userId = optionalCurrentUserId(authentication);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userId;
    }
}
