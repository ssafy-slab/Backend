package com.ssafy.ssafy_slap.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityCommentUpdateRequest(
        @NotBlank @Size(max = 1000) String content
) {
    public String normalizedContent() {
        if (content == null) {
            return null;
        }
        String normalized = content.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
