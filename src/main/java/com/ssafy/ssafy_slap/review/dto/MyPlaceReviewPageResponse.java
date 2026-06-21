package com.ssafy.ssafy_slap.review.dto;

import java.util.List;

public record MyPlaceReviewPageResponse(
        List<MyPlaceReviewResponse> content,
        long totalElements,
        int page,
        int size,
        int totalPages
) {
}
