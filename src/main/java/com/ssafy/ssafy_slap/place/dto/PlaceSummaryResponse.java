package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceSummaryResponse(
        Long placeId,
        String placeName,
        String category,
        Long regionId,
        String regionName,
        String regionFullName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String description,
        String imageUrl,
        String thumbnailUrl,
        String displayImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
