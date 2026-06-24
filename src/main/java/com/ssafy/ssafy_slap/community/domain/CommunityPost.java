package com.ssafy.ssafy_slap.community.domain;

import java.time.LocalDateTime;
import java.util.List;

public class CommunityPost {

    private Long postId;
    private Long userId;
    private String authorNickname;
    private Long tripId;
    private Long placeId;
    private String placeName;
    private String category;
    private String title;
    private String content;
    private String imageUrl;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean likedByMe;
    private Boolean mine;
    private List<CommunityPostCell> cells;

    public CommunityPost() {
    }

    public CommunityPost(
            Long postId,
            Long userId,
            String authorNickname,
            Long tripId,
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
            Boolean mine
    ) {
        this.postId = postId;
        this.userId = userId;
        this.authorNickname = authorNickname;
        this.tripId = tripId;
        this.placeId = placeId;
        this.placeName = placeName;
        this.category = category;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likedByMe = likedByMe;
        this.mine = mine;
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorNickname() { return authorNickname; }
    public void setAuthorNickname(String authorNickname) { this.authorNickname = authorNickname; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getLikedByMe() { return likedByMe; }
    public void setLikedByMe(Boolean likedByMe) { this.likedByMe = likedByMe; }
    public Boolean getMine() { return mine; }
    public void setMine(Boolean mine) { this.mine = mine; }
    public List<CommunityPostCell> getCells() { return cells; }
    public void setCells(List<CommunityPostCell> cells) { this.cells = cells; }
}
