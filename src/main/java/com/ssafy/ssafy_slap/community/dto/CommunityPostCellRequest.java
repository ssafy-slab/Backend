package com.ssafy.ssafy_slap.community.dto;

import jakarta.validation.constraints.Size;

public record CommunityPostCellRequest(
        String cellType,
        @Size(max = 5000) String textContent,
        @Size(max = 1000) String imageUrl
) {
    public String normalizedCellType() {
        return normalize(cellType);
    }

    public String normalizedTextContent() {
        return normalize(textContent);
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
