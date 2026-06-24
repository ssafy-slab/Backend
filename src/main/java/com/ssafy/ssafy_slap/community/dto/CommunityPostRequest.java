package com.ssafy.ssafy_slap.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CommunityPostRequest(
        @NotBlank @Size(max = 100) String category,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 5000) String content,
        @Size(max = 1000) String imageUrl,
        Long placeId,
        List<CommunityPostCellRequest> cells
) {
    public CommunityPostRequest(
            String category,
            String title,
            String content,
            String imageUrl,
            Long placeId
    ) {
        this(category, title, content, imageUrl, placeId, null);
    }

    public String normalizedCategory() {
        return normalize(category);
    }

    public String normalizedTitle() {
        return normalize(title);
    }

    public String normalizedContent() {
        return normalize(content);
    }

    public String normalizedImageUrl() {
        return normalize(imageUrl);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
