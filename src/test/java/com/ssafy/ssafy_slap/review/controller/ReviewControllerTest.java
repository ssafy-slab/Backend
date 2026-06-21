package com.ssafy.ssafy_slap.review.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewRequest;
import com.ssafy.ssafy_slap.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReviewControllerTest {

    private final ReviewService reviewService = mock(ReviewService.class);
    private final ReviewController controller = new ReviewController(reviewService);

    @Test
    void allowsAnonymousReviewLookup() {
        controller.getReviews(10L, null);

        verify(reviewService).getReviews(10L, null);
    }

    @Test
    void passesAuthenticatedUserToCreate() {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );

        controller.create(10L, authentication, new PlaceReviewRequest(5, null));

        verify(reviewService).create(10L, 7L, new PlaceReviewRequest(5, null));
    }

    @Test
    void rejectsAnonymousMutation() {
        assertThatThrownBy(() -> controller.delete(10L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
