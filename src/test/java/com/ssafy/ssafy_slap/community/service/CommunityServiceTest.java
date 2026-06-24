package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;
import com.ssafy.ssafy_slap.community.domain.CommunityPostCell;
import com.ssafy.ssafy_slap.community.dto.CommunityPostCellRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentUpdateRequest;
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
    void createsCellsFromLegacyContentAndImageWhenCellsAreMissing() {
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
                "Good beach",
                "  Visit before sunset.  ",
                "  https://example.com/a.jpg  ",
                3L
        ));

        verify(communityMapper).insertPostCells(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.argThat(cells ->
                cells.size() == 2
                        && cells.get(0).getSortOrder() == 1
                        && cells.get(0).getCellType().equals("TEXT")
                        && cells.get(0).getTextContent().equals("Visit before sunset.")
                        && cells.get(1).getSortOrder() == 2
                        && cells.get(1).getCellType().equals("IMAGE")
                        && cells.get(1).getImageUrl().equals("https://example.com/a.jpg")
        ));
    }

    @Test
    void createsPostWithOrderedCells() {
        var saved = detail(1L, "Good beach", 0L, 0L, false);
        when(communityMapper.findPostById(1L, 7L)).thenReturn(saved);
        doAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setPostId(1L);
            return null;
        }).when(communityMapper).insertPost(org.mockito.ArgumentMatchers.any(CommunityPost.class));

        communityService.createPost(7L, new CommunityPostRequest(
                "PLACE_REVIEW",
                "Good beach",
                null,
                null,
                null,
                List.of(
                        new CommunityPostCellRequest("TEXT", "  First story  ", null, "CENTER"),
                        new CommunityPostCellRequest("IMAGE", null, "  https://example.com/a.jpg  ", "RIGHT"),
                        new CommunityPostCellRequest("TEXT", "Second story", null, null)
                )
        ));

        verify(communityMapper).insertPost(org.mockito.ArgumentMatchers.argThat(post ->
                post.getContent().equals("First story")
                        && post.getImageUrl().equals("https://example.com/a.jpg")
        ));
        verify(communityMapper).insertPostCells(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.argThat(cells ->
                cells.size() == 3
                        && cells.get(0).getSortOrder() == 1
                        && cells.get(0).getCellType().equals("TEXT")
                        && cells.get(0).getTextContent().equals("First story")
                        && cells.get(0).getAlignment().equals("CENTER")
                        && cells.get(1).getSortOrder() == 2
                        && cells.get(1).getCellType().equals("IMAGE")
                        && cells.get(1).getImageUrl().equals("https://example.com/a.jpg")
                        && cells.get(1).getAlignment().equals("RIGHT")
                        && cells.get(2).getSortOrder() == 3
                        && cells.get(2).getTextContent().equals("Second story")
                        && cells.get(2).getAlignment().equals("LEFT")
        ));
    }

    @Test
    void rejectsPostWithMoreThanFiveCells() {
        var cells = List.of(
                new CommunityPostCellRequest("TEXT", "one", null),
                new CommunityPostCellRequest("TEXT", "two", null),
                new CommunityPostCellRequest("TEXT", "three", null),
                new CommunityPostCellRequest("TEXT", "four", null),
                new CommunityPostCellRequest("TEXT", "five", null),
                new CommunityPostCellRequest("TEXT", "six", null)
        );

        assertThatThrownBy(() -> communityService.createPost(7L, new CommunityPostRequest(
                "PLACE_REVIEW",
                "Good beach",
                null,
                null,
                null,
                cells
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");

        verifyNoInteractions(imageStorageService);
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
    void returnsDetailWithPersistedCellsInOrder() {
        var post = detail(1L, "Good beach", 2L, 1L, false);
        when(communityMapper.findPostById(1L, 7L)).thenReturn(post);
        when(communityMapper.findPostCells(1L)).thenReturn(List.of(
                new CommunityPostCell(10L, 1L, 1, "TEXT", "First story", null, "CENTER"),
                new CommunityPostCell(11L, 1L, 2, "IMAGE", null, "https://example.com/a.jpg", "RIGHT"),
                new CommunityPostCell(12L, 1L, 3, "TEXT", "Second story", null, "LEFT"),
                new CommunityPostCell(13L, 1L, 4, "IMAGE", null, "https://example.com/b.jpg", "CENTER")
        ));

        var result = communityService.findPost(1L, 7L);

        assertThat(result.cells()).extracting("cellType")
                .containsExactly("TEXT", "IMAGE", "TEXT", "IMAGE");
        assertThat(result.cells()).extracting("textContent")
                .containsExactly("First story", null, "Second story", null);
        assertThat(result.cells()).extracting("imageUrl")
                .containsExactly(null, "https://example.com/a.jpg", null, "https://example.com/b.jpg");
        assertThat(result.cells()).extracting("alignment")
                .containsExactly("CENTER", "RIGHT", "LEFT", "CENTER");
    }

    @Test
    void rejectsUnsupportedCellAlignment() {
        assertThatThrownBy(() -> communityService.createPost(7L, new CommunityPostRequest(
                "PLACE_REVIEW",
                "Good beach",
                null,
                null,
                null,
                List.of(new CommunityPostCellRequest("TEXT", "story", null, "JUSTIFY"))
        )))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
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
    void createsReplyOnlyForActiveCommentInSamePost() {
        when(communityMapper.existsPost(1L)).thenReturn(true);
        when(communityMapper.existsActiveCommentInPost(1L, 9L)).thenReturn(true);

        communityService.createComment(1L, 7L, new CommunityCommentRequest("  Reply  ", 9L));

        verify(communityMapper).insertComment(1L, 7L, "Reply", 9L);
    }

    @Test
    void rejectsReplyWhenParentCommentIsMissingOrDeleted() {
        when(communityMapper.existsPost(1L)).thenReturn(true);
        when(communityMapper.existsActiveCommentInPost(1L, 9L)).thenReturn(false);

        assertThatThrownBy(() -> communityService.createComment(
                1L,
                7L,
                new CommunityCommentRequest("Reply", 9L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void updatesOwnActiveCommentAndReturnsRefreshedComments() {
        CommunityCommentResponse updated = commentResponse("Edited", true, true);
        when(communityMapper.updateComment(9L, 7L, "Edited")).thenReturn(1);
        when(communityMapper.findCommentPostId(9L)).thenReturn(1L);
        when(communityMapper.existsPost(1L)).thenReturn(true);
        when(communityMapper.findComments(1L, 7L)).thenReturn(List.of(updated));

        var result = communityService.updateComment(
                9L,
                7L,
                new CommunityCommentUpdateRequest("  Edited  ")
        );

        verify(communityMapper).updateComment(9L, 7L, "Edited");
        assertThat(result).containsExactly(updated);
    }

    @Test
    void rejectsUpdatingAnotherUsersCommentWithForbidden() {
        when(communityMapper.updateComment(9L, 7L, "Edited")).thenReturn(0);
        when(communityMapper.existsActiveComment(9L)).thenReturn(true);

        assertThatThrownBy(() -> communityService.updateComment(
                9L,
                7L,
                new CommunityCommentUpdateRequest("Edited")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void rejectsDeletingAnotherUsersCommentWithForbidden() {
        when(communityMapper.deleteComment(9L, 7L)).thenReturn(0);
        when(communityMapper.existsActiveComment(9L)).thenReturn(true);

        assertThatThrownBy(() -> communityService.deleteComment(9L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void deletesPostAndAttemptsS3ImageDeletionAfterDbDelete() {
        String imageUrl = "https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/a.jpg";
        when(communityMapper.findPostImageUrl(1L, 7L)).thenReturn(imageUrl);
        when(communityMapper.findPostCellImageUrls(1L)).thenReturn(List.of(
                "https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/b.jpg",
                "https://example.com/external.jpg"
        ));
        when(communityMapper.deletePost(1L, 7L)).thenReturn(1);

        communityService.deletePost(1L, 7L);

        verify(communityMapper).deletePost(1L, 7L);
        verify(imageStorageService).deleteIfOwnedS3Image(imageUrl);
        verify(imageStorageService).deleteIfOwnedS3Image("https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/b.jpg");
        verify(imageStorageService).deleteIfOwnedS3Image("https://example.com/external.jpg");
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

    private CommunityCommentResponse commentResponse(String content, boolean mine, boolean edited) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 24, 10, 0);
        return new CommunityCommentResponse(
                9L,
                1L,
                7L,
                "traveler",
                null,
                null,
                content,
                createdAt,
                edited ? createdAt.plusMinutes(1) : createdAt,
                mine,
                false,
                edited
        );
    }
}
