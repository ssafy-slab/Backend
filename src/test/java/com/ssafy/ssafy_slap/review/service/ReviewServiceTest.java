package com.ssafy.ssafy_slap.review.service;

import com.ssafy.ssafy_slap.review.dto.PlaceReviewRequest;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewResponse;
import com.ssafy.ssafy_slap.review.dto.MyPlaceReviewResponse;
import com.ssafy.ssafy_slap.review.mapper.ReviewMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private final ReviewMapper reviewMapper = mock(ReviewMapper.class);
    private final ReviewService reviewService = new ReviewService(reviewMapper);

    @Test
    void createsRatingOnlyReviewAndReturnsUpdatedSummary() {
        PlaceReviewResponse saved = review(1L, 7L, 5, null);
        when(reviewMapper.existsPlace(10L)).thenReturn(true);
        when(reviewMapper.existsByPlaceAndUser(10L, 7L)).thenReturn(false);
        when(reviewMapper.findByPlaceAndUser(10L, 7L)).thenReturn(saved);
        when(reviewMapper.findByPlace(10L, 7L)).thenReturn(List.of(saved));
        when(reviewMapper.averageRating(10L)).thenReturn(new BigDecimal("5.0"));
        when(reviewMapper.countReviews(10L)).thenReturn(1L);

        var result = reviewService.create(10L, 7L, new PlaceReviewRequest(5, "  "));

        verify(reviewMapper).insert(10L, 7L, 5, null);
        assertThat(result.averageRating()).isEqualByComparingTo("5.0");
        assertThat(result.myReview()).isEqualTo(saved);
    }

    @Test
    void rejectsDuplicateReview() {
        when(reviewMapper.existsPlace(10L)).thenReturn(true);
        when(reviewMapper.existsByPlaceAndUser(10L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(10L, 7L, new PlaceReviewRequest(4, "good")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void rejectsRatingOutsideOneToFive() {
        assertThatThrownBy(() -> reviewService.create(10L, 7L, new PlaceReviewRequest(0, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void updatesAndDeletesOnlyExistingOwnReview() {
        PlaceReviewResponse updated = review(1L, 7L, 3, "updated");
        when(reviewMapper.existsPlace(10L)).thenReturn(true);
        when(reviewMapper.existsByPlaceAndUser(10L, 7L)).thenReturn(true);
        when(reviewMapper.findByPlaceAndUser(10L, 7L)).thenReturn(updated);
        when(reviewMapper.findByPlace(10L, 7L)).thenReturn(List.of(updated));
        when(reviewMapper.averageRating(10L)).thenReturn(new BigDecimal("3.0"));
        when(reviewMapper.countReviews(10L)).thenReturn(1L);

        reviewService.update(10L, 7L, new PlaceReviewRequest(3, " updated "));
        reviewService.delete(10L, 7L);

        verify(reviewMapper).update(10L, 7L, 3, "updated");
        verify(reviewMapper).delete(10L, 7L);
    }

    @Test
    void returnsCurrentUsersReviews() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 21, 12, 0);
        var item = new MyPlaceReviewResponse(
                1L, 10L, "place", "category", "/thumb.jpg",
                5, "great", now, now
        );
        when(reviewMapper.findByUser(7L, 10, 10)).thenReturn(List.of(item));
        when(reviewMapper.countByUser(7L)).thenReturn(21L);

        var result = reviewService.getMyReviews(7L, 1, 10);

        assertThat(result.content()).containsExactly(item);
        assertThat(result.totalElements()).isEqualTo(21);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    private PlaceReviewResponse review(Long reviewId, Long userId, int rating, String content) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 21, 12, 0);
        return new PlaceReviewResponse(reviewId, userId, "traveler", rating, content, now, now, true);
    }
}
