package com.ssafy.ssafy_slap.community.dto;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;

import java.time.LocalDateTime;
import java.util.List;

public record CommunityPostDetailResponse(
        Long postId,
        Long userId,
        String authorNickname,
        Long placeId,
        String placeName,
        String category,
        String title,
        String content,
        String imageUrl,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean likedByMe,
        Boolean mine,
        List<CommunityPostCellResponse> cells
) {
    public static CommunityPostDetailResponse from(CommunityPost post) {
        return new CommunityPostDetailResponse(
                post.getPostId(),
                post.getUserId(),
                post.getAuthorNickname(),
                post.getPlaceId(),
                post.getPlaceName(),
                post.getCategory(),
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getLikedByMe(),
                post.getMine(),
                post.getCells() == null
                        ? List.of()
                        : post.getCells().stream().map(CommunityPostCellResponse::from).toList()
        );
    }
}
