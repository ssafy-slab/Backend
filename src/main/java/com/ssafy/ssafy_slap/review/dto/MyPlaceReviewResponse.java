package com.ssafy.ssafy_slap.review.dto;

import java.time.LocalDateTime;

public record MyPlaceReviewResponse(
        Long reviewId,
        Long placeId,
        String placeName,
        String category,
        String thumbnailImageUrl,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
