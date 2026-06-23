package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityPostSummaryResponse;
import com.ssafy.ssafy_slap.community.mapper.CommunityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommunityServiceTest {

    private final CommunityMapper communityMapper = mock(CommunityMapper.class);
    private final CommunityImageStorageService imageStorageService = mock(CommunityImageStorageService.class);
    private final CommunityService communityService = new CommunityService(communityMapper, imageStorageService);

    @Test
    void createsPostWithNormalizedContent() {
        var saved = detail(1L, "Good beach", 0L, 0L, false);
        when(communityMapper.existsPlace(3L)).thenReturn(true);
        when(communityMapper.findPostById(1L, 7L)).thenReturn(saved);
        doAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setPostId(1L);
            return null;
        }).when(communityMapper).insertPost(org.mockito.ArgumentMatchers.any(CommunityPost.class));

        communityService.createPost(7L, new CommunityPostRequest(
                "PLACE_REVIEW",
                "  Good beach  ",
                "  Visit before sunset.  ",
                "  https://example.com/a.jpg  ",
                3L
        ));

        verify(communityMapper).insertPost(org.mockito.ArgumentMatchers.argThat(post ->
                post.getUserId().equals(7L)
                        && post.getPlaceId().equals(3L)
                        && post.getTitle().equals("Good beach")
                        && post.getContent().equals("Visit before sunset.")
                        && post.getImageUrl().equals("https://example.com/a.jpg")
        ));
    }

    @Test
    void listsActivePostsWithSafePaging() {
        var post = summary(1L, "Good beach", 2L, 1L, false);
        when(communityMapper.findPosts(null, "beach", "popular", 50, 0, 7L)).thenReturn(List.of(post));

        var result = communityService.findPosts(null, " beach ", "popular", -1, 100, 7L);

        assertThat(result).containsExactly(post);
    }

    @Test
    void returnsDetailAndIncrementsViewCount() {
        var post = detail(1L, "Good beach", 2L, 1L, false);
        when(communityMapper.findPostById(1L, 7L)).thenReturn(post);

        var result = communityService.findPost(1L, 7L);

        verify(communityMapper).incrementViewCount(1L);
        assertThat(result.postId()).isEqualTo(1L);
    }

    @Test
    void togglesLikeForExistingPost() {
        when(communityMapper.existsPost(1L)).thenReturn(true);
        when(communityMapper.existsLike(1L, 7L)).thenReturn(false);

        communityService.toggleLike(1L, 7L);

        verify(communityMapper).insertLike(1L, 7L);
    }

    @Test
    void createsCommentForExistingPost() {
        when(communityMapper.existsPost(1L)).thenReturn(true);

        communityService.createComment(1L, 7L, new CommunityCommentRequest("  Nice tip  ", null));

        verify(communityMapper).insertComment(1L, 7L, "Nice tip", null);
    }

    @Test
    void deletesPostAndAttemptsS3ImageDeletionAfterDbDelete() {
        String imageUrl = "https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/a.jpg";
        when(communityMapper.findPostImageUrl(1L, 7L)).thenReturn(imageUrl);
        when(communityMapper.deletePost(1L, 7L)).thenReturn(1);

        communityService.deletePost(1L, 7L);

        verify(communityMapper).deletePost(1L, 7L);
        verify(imageStorageService).deleteIfOwnedS3Image(imageUrl);
    }

    @Test
    void doesNotDeleteS3ImageWhenPostDeleteFails() {
        when(communityMapper.deletePost(1L, 7L)).thenReturn(0);
        when(communityMapper.existsPost(1L)).thenReturn(false);

        assertThatThrownBy(() -> communityService.deletePost(1L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verifyNoInteractions(imageStorageService);
    }

    @Test
    void rejectsPostWithoutTitle() {
        assertThatThrownBy(() -> communityService.createPost(7L, new CommunityPostRequest("TIP", " ", "body", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    private CommunityPostSummaryResponse summary(Long postId, String title, long likes, long comments, boolean liked) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 10, 0);
        return new CommunityPostSummaryResponse(
                postId, 7L, "traveler", 3L, "Beach", "PLACE_REVIEW", title,
                "excerpt", "https://example.com/a.jpg", likes, comments, 4L, now, now, liked, true
        );
    }

    private CommunityPost detail(Long postId, String title, long likes, long comments, boolean liked) {
        LocalDateTime now = LocalDateTime.of(2026, 6, 22, 10, 0);
        return new CommunityPost(
                postId, 7L, "traveler", null, 3L, "Beach", "PLACE_REVIEW", title,
                "content", "https://example.com/a.jpg", likes, comments, 4L, now, now, liked, true
        );
    }
}
