package com.ssafy.ssafy_slap.community.mapper;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;
import com.ssafy.ssafy_slap.community.domain.CommunityPostCell;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    boolean existsPlace(@Param("placeId") Long placeId);

    boolean existsPost(@Param("postId") Long postId);

    boolean isPostOwner(@Param("postId") Long postId, @Param("userId") Long userId);

    void insertPost(@Param("post") CommunityPost post);

    void insertPostCells(@Param("postId") Long postId, @Param("cells") List<CommunityPostCell> cells);

    List<CommunityPostSummaryResponse> findPosts(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("size") int size,
            @Param("offset") int offset,
            @Param("currentUserId") Long currentUserId
    );

    CommunityPost findPostById(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

    List<CommunityPostCell> findPostCells(@Param("postId") Long postId);

    String findPostImageUrl(@Param("postId") Long postId, @Param("userId") Long userId);

    List<String> findPostCellImageUrls(@Param("postId") Long postId);

    void incrementViewCount(@Param("postId") Long postId);

    int updatePost(@Param("post") CommunityPost post, @Param("userId") Long userId);

    void deletePostCells(@Param("postId") Long postId);

    int deletePost(@Param("postId") Long postId, @Param("userId") Long userId);

    boolean existsLike(@Param("postId") Long postId, @Param("userId") Long userId);

    void insertLike(@Param("postId") Long postId, @Param("userId") Long userId);

    void deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);

    boolean existsBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    void insertBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    void deleteBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    List<CommunityPostSummaryResponse> findBookmarkedPosts(
            @Param("userId") Long userId,
            @Param("size") int size,
            @Param("offset") int offset
    );

    List<CommunityCommentResponse> findComments(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);

    boolean existsActiveCommentInPost(@Param("postId") Long postId, @Param("commentId") Long commentId);

    boolean existsActiveComment(@Param("commentId") Long commentId);

    Long findCommentPostId(@Param("commentId") Long commentId);

    void insertComment(
            @Param("postId") Long postId,
            @Param("userId") Long userId,
            @Param("content") String content,
            @Param("parentCommentId") Long parentCommentId
    );

    int updateComment(
            @Param("commentId") Long commentId,
            @Param("userId") Long userId,
            @Param("content") String content
    );

    int deleteComment(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
