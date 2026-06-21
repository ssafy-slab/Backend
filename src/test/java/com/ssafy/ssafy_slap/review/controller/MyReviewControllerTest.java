package com.ssafy.ssafy_slap.review.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MyReviewControllerTest {

    private final ReviewService reviewService = mock(ReviewService.class);
    private final MyReviewController controller = new MyReviewController(reviewService);

    @Test
    void loadsAuthenticatedUsersReviews() {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"), null, List.of()
        );

        controller.getMyReviews(authentication, 2, 10);

        verify(reviewService).getMyReviews(7L, 2, 10);
    }

    @Test
    void rejectsAnonymousLookup() {
        assertThatThrownBy(() -> controller.getMyReviews(null, 0, 10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
