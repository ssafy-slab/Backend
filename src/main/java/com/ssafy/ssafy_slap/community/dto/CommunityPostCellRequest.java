package com.ssafy.ssafy_slap.community.dto;

import jakarta.validation.constraints.Size;

public record CommunityPostCellRequest(
        String cellType,
        @Size(max = 5000) String textContent,
        @Size(max = 1000) String imageUrl,
        String alignment,
        Integer fontSizePx,
        Boolean bold
) {
    public CommunityPostCellRequest(String cellType, String textContent, String imageUrl) {
        this(cellType, textContent, imageUrl, null, null, null);
    }

    public CommunityPostCellRequest(String cellType, String textContent, String imageUrl, String alignment) {
        this(cellType, textContent, imageUrl, alignment, null, null);
    }

    public String normalizedCellType() {
        return normalize(cellType);
    }

    public String normalizedTextContent() {
        return normalize(textContent);
    }

    public String normalizedImageUrl() {
        return normalize(imageUrl);
    }

    public String normalizedAlignment() {
        return normalize(alignment);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
