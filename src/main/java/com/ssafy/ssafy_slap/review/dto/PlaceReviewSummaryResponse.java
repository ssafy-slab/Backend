package com.ssafy.ssafy_slap.review.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlaceReviewSummaryResponse(
        BigDecimal averageRating,
        long reviewCount,
        List<PlaceReviewResponse> reviews,
        PlaceReviewResponse myReview
) {
}
