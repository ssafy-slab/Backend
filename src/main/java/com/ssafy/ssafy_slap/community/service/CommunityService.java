package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostDetailResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityPostSummaryResponse;
import com.ssafy.ssafy_slap.community.mapper.CommunityMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private static final Set<String> SORTS = Set.of("latest", "popular", "comments");

    private final CommunityMapper communityMapper;
    private final CommunityImageStorageService imageStorageService;

    public CommunityService(CommunityMapper communityMapper, CommunityImageStorageService imageStorageService) {
        this.communityMapper = communityMapper;
        this.imageStorageService = imageStorageService;
    }

    public List<CommunityPostSummaryResponse> findPosts(
            String category,
            String keyword,
            String sort,
            Integer requestedPage,
            Integer requestedSize,
            Long currentUserId
    ) {
        int page = requestedPage == null || requestedPage < 0 ? 0 : requestedPage;
        int size = requestedSize == null || requestedSize < 1 ? 20 : Math.min(requestedSize, 50);
        String normalizedSort = SORTS.contains(sort) ? sort : "latest";
        return communityMapper.findPosts(
                normalize(category),
                normalize(keyword),
                normalizedSort,
                size,
                page * size,
                currentUserId
        );
    }

    @Transactional
    public CommunityPostDetailResponse findPost(Long postId, Long currentUserId) {
        validatePostId(postId);
        CommunityPost post = communityMapper.findPostById(postId, currentUserId);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
        communityMapper.incrementViewCount(postId);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public CommunityPostDetailResponse createPost(Long userId, CommunityPostRequest request) {
        validateUser(userId);
        validatePostRequest(request);
        validatePlace(request.placeId());

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setPlaceId(request.placeId());
        post.setCategory(request.normalizedCategory());
        post.setTitle(request.normalizedTitle());
        post.setContent(request.normalizedContent());
        post.setImageUrl(request.normalizedImageUrl());
        communityMapper.insertPost(post);
        return findPost(post.getPostId(), userId);
    }

    @Transactional
    public CommunityPostDetailResponse updatePost(Long postId, Long userId, CommunityPostRequest request) {
        validatePostId(postId);
        validateUser(userId);
        validatePostRequest(request);
        validatePlace(request.placeId());

        CommunityPost post = new CommunityPost();
        post.setPostId(postId);
        post.setPlaceId(request.placeId());
        post.setCategory(request.normalizedCategory());
        post.setTitle(request.normalizedTitle());
        post.setContent(request.normalizedContent());
        post.setImageUrl(request.normalizedImageUrl());

        int updated = communityMapper.updatePost(post, userId);
        if (updated == 0) {
            throwMissingOrForbidden(postId, userId);
        }
        return findPost(postId, userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        validatePostId(postId);
        validateUser(userId);
        String imageUrl = communityMapper.findPostImageUrl(postId, userId);
        int deleted = communityMapper.deletePost(postId, userId);
        if (deleted == 0) {
            throwMissingOrForbidden(postId, userId);
        }
        imageStorageService.deleteIfOwnedS3Image(imageUrl);
    }

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        validatePostId(postId);
        validateUser(userId);
        ensurePostExists(postId);
        if (communityMapper.existsLike(postId, userId)) {
            communityMapper.deleteLike(postId, userId);
        } else {
            communityMapper.insertLike(postId, userId);
        }
    }

    public List<CommunityCommentResponse> findComments(Long postId, Long currentUserId) {
        validatePostId(postId);
        ensurePostExists(postId);
        return communityMapper.findComments(postId, currentUserId);
    }

    @Transactional
    public List<CommunityCommentResponse> createComment(Long postId, Long userId, CommunityCommentRequest request) {
        validatePostId(postId);
        validateUser(userId);
        if (request == null || request.normalizedContent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        ensurePostExists(postId);
        communityMapper.insertComment(postId, userId, request.normalizedContent(), request.parentCommentId());
        return findComments(postId, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        if (commentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commentId is required");
        }
        validateUser(userId);
        if (communityMapper.deleteComment(commentId, userId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
    }

    private void validatePostRequest(CommunityPostRequest request) {
        if (request == null || request.normalizedTitle() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (request.normalizedCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required");
        }
    }

    private void validatePlace(Long placeId) {
        if (placeId != null && !communityMapper.existsPlace(placeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
    }

    private void ensurePostExists(Long postId) {
        if (!communityMapper.existsPost(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    private void throwMissingOrForbidden(Long postId, Long userId) {
        if (communityMapper.existsPost(postId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author can modify post");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
    }

    private void validatePostId(Long postId) {
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "postId is required");
        }
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
