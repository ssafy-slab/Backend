package com.ssafy.ssafy_slap.review.dto;

import java.time.LocalDateTime;

public record PlaceReviewResponse(
        Long reviewId,
        Long userId,
        String authorNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean mine
) {
}
