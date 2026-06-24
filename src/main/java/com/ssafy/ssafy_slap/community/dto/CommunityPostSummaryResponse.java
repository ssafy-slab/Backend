package com.ssafy.ssafy_slap.community.dto;

import java.time.LocalDateTime;

public record CommunityPostSummaryResponse(
        Long postId,
        Long userId,
        String authorNickname,
        Long placeId,
        String placeName,
        String category,
        String title,
        String excerpt,
        String imageUrl,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean likedByMe,
        Boolean bookmarkedByMe,
        Boolean mine
) {
}
