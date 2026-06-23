package com.ssafy.ssafy_slap.community.dto;

import java.time.LocalDateTime;

public record CommunityCommentResponse(
        Long commentId,
        Long postId,
        Long commenterUserId,
        String authorNickname,
        Long parentCommentId,
        String replyToNickname,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean mine,
        Boolean deleted,
        Boolean edited
) {
}
