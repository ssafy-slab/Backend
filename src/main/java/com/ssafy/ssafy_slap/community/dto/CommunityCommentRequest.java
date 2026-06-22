package com.ssafy.ssafy_slap.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommunityCommentRequest(
        @NotBlank @Size(max = 1000) String content,
        Long parentCommentId
) {
    public String normalizedContent() {
        if (content == null) {
            return null;
        }
        String normalized = content.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
