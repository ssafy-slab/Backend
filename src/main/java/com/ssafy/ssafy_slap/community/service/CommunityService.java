package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.domain.CommunityPost;
import com.ssafy.ssafy_slap.community.domain.CommunityPostCell;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentUpdateRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityPostCellRequest;
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
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private static final Set<String> SORTS = Set.of("latest", "popular", "comments");
    private static final String CELL_TYPE_TEXT = "TEXT";
    private static final String CELL_TYPE_IMAGE = "IMAGE";
    private static final String DEFAULT_ALIGNMENT = "LEFT";
    private static final Set<String> CELL_ALIGNMENTS = Set.of("LEFT", "CENTER", "RIGHT");
    private static final int DEFAULT_FONT_SIZE_PX = 14;
    private static final int IMAGE_FONT_SIZE_PX = 0;
    private static final Set<Integer> CELL_FONT_SIZE_PX = Set.of(14, 16, 18, 20, 24, 28, 32);
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
        post.setCells(resolvePostCells(post));
        communityMapper.incrementViewCount(postId);
        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        return CommunityPostDetailResponse.from(post);
    }

    @Transactional
    public CommunityPostDetailResponse createPost(Long userId, CommunityPostRequest request) {
        validateUser(userId);
        validatePostRequest(request);
        validatePlace(request.placeId());
        List<CommunityPostCell> cells = normalizeCells(request);

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setPlaceId(request.placeId());
        post.setCategory(request.normalizedCategory());
        post.setTitle(request.normalizedTitle());
        post.setContent(firstTextContent(cells, request.normalizedContent()));
        post.setImageUrl(firstImageUrl(cells, request.normalizedImageUrl()));
        communityMapper.insertPost(post);
        if (!cells.isEmpty()) {
            communityMapper.insertPostCells(post.getPostId(), cells);
        }
        return findPost(post.getPostId(), userId);
    }

    @Transactional
    public CommunityPostDetailResponse updatePost(Long postId, Long userId, CommunityPostRequest request) {
        validatePostId(postId);
        validateUser(userId);
        validatePostRequest(request);
        validatePlace(request.placeId());
        List<CommunityPostCell> cells = normalizeCells(request);

        CommunityPost post = new CommunityPost();
        post.setPostId(postId);
        post.setPlaceId(request.placeId());
        post.setCategory(request.normalizedCategory());
        post.setTitle(request.normalizedTitle());
        post.setContent(firstTextContent(cells, request.normalizedContent()));
        post.setImageUrl(firstImageUrl(cells, request.normalizedImageUrl()));

        int updated = communityMapper.updatePost(post, userId);
        if (updated == 0) {
            throwMissingOrForbidden(postId, userId);
        }
        communityMapper.deletePostCells(postId);
        if (!cells.isEmpty()) {
            communityMapper.insertPostCells(postId, cells);
        }
        return findPost(postId, userId);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        validatePostId(postId);
        validateUser(userId);
        String imageUrl = communityMapper.findPostImageUrl(postId, userId);
        List<String> cellImageUrls = communityMapper.findPostCellImageUrls(postId);
        int deleted = communityMapper.deletePost(postId, userId);
        if (deleted == 0) {
            throwMissingOrForbidden(postId, userId);
        }
        imageStorageService.deleteIfOwnedS3Image(imageUrl);
        if (cellImageUrls != null) {
            cellImageUrls.forEach(imageStorageService::deleteIfOwnedS3Image);
        }
    }

    @Transactional
    public void likePost(Long postId, Long userId) {
        validatePostId(postId);
        validateUser(userId);
        ensurePostExists(postId);
        communityMapper.insertLike(postId, userId);
    }

    @Transactional
    public void removeLike(Long postId, Long userId) {
        validatePostId(postId);
        validateUser(userId);
        ensurePostExists(postId);
        communityMapper.deleteLike(postId, userId);
    }

    public List<CommunityPostSummaryResponse> findLikedPosts(
            Long userId,
            Integer requestedPage,
            Integer requestedSize
    ) {
        validateUser(userId);
        int page = requestedPage == null || requestedPage < 0 ? 0 : requestedPage;
        int size = requestedSize == null || requestedSize < 1 ? 20 : Math.min(requestedSize, 50);
        return communityMapper.findLikedPosts(userId, size, page * size);
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
        if (request.parentCommentId() != null
                && !communityMapper.existsActiveCommentInPost(postId, request.parentCommentId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found");
        }
        communityMapper.insertComment(postId, userId, request.normalizedContent(), request.parentCommentId());
        return findComments(postId, userId);
    }

    @Transactional
    public List<CommunityCommentResponse> updateComment(
            Long commentId,
            Long userId,
            CommunityCommentUpdateRequest request
    ) {
        validateCommentId(commentId);
        validateUser(userId);
        if (request == null || request.normalizedContent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "content is required");
        }
        if (communityMapper.updateComment(commentId, userId, request.normalizedContent()) == 0) {
            throwMissingOrForbiddenComment(commentId);
        }
        Long postId = communityMapper.findCommentPostId(commentId);
        if (postId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
        }
        return findComments(postId, userId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        validateCommentId(commentId);
        validateUser(userId);
        if (communityMapper.deleteComment(commentId, userId) == 0) {
            throwMissingOrForbiddenComment(commentId);
        }
    }

    private void throwMissingOrForbiddenComment(Long commentId) {
        if (communityMapper.existsActiveComment(commentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only comment author can modify comment");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
    }

    private void validatePostRequest(CommunityPostRequest request) {
        if (request == null || request.normalizedTitle() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        if (request.normalizedCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required");
        }
    }

    private List<CommunityPostCell> normalizeCells(CommunityPostRequest request) {
        if (request.cells() == null || request.cells().isEmpty()) {
            return legacyCells(request);
        }
        return IntStream.range(0, request.cells().size())
                .mapToObj((index) -> toCell(request.cells().get(index), index + 1))
                .toList();
    }

    private CommunityPostCell toCell(CommunityPostCellRequest request, int sortOrder) {
        if (request == null || request.normalizedCellType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cellType is required");
        }
        String cellType = request.normalizedCellType().toUpperCase();
        String alignment = normalizeAlignment(request.normalizedAlignment());
        if (CELL_TYPE_TEXT.equals(cellType)) {
            if (request.normalizedTextContent() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "textContent is required");
            }
            return new CommunityPostCell(
                    null,
                    null,
                    sortOrder,
                    cellType,
                    request.normalizedTextContent(),
                    null,
                    alignment,
                    normalizeFontSizePx(request.fontSizePx()),
                    Boolean.TRUE.equals(request.bold())
            );
        }
        if (CELL_TYPE_IMAGE.equals(cellType)) {
            if (request.normalizedImageUrl() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl is required");
            }
            return new CommunityPostCell(null, null, sortOrder, cellType, null, request.normalizedImageUrl(), alignment, IMAGE_FONT_SIZE_PX, false);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cellType must be TEXT or IMAGE");
    }

    private List<CommunityPostCell> legacyCells(CommunityPostRequest request) {
        java.util.ArrayList<CommunityPostCell> cells = new java.util.ArrayList<>();
        int sortOrder = 1;
        if (request.normalizedContent() != null) {
            cells.add(new CommunityPostCell(null, null, sortOrder++, CELL_TYPE_TEXT, request.normalizedContent(), null, DEFAULT_ALIGNMENT, DEFAULT_FONT_SIZE_PX, false));
        }
        if (request.normalizedImageUrl() != null) {
            cells.add(new CommunityPostCell(null, null, sortOrder, CELL_TYPE_IMAGE, null, request.normalizedImageUrl(), DEFAULT_ALIGNMENT, IMAGE_FONT_SIZE_PX, false));
        }
        return cells;
    }

    private String firstTextContent(List<CommunityPostCell> cells, String fallback) {
        return cells.stream()
                .filter((cell) -> CELL_TYPE_TEXT.equals(cell.getCellType()))
                .map(CommunityPostCell::getTextContent)
                .findFirst()
                .orElse(fallback);
    }

    private String firstImageUrl(List<CommunityPostCell> cells, String fallback) {
        return cells.stream()
                .filter((cell) -> CELL_TYPE_IMAGE.equals(cell.getCellType()))
                .map(CommunityPostCell::getImageUrl)
                .findFirst()
                .orElse(fallback);
    }

    private List<CommunityPostCell> resolvePostCells(CommunityPost post) {
        List<CommunityPostCell> cells = communityMapper.findPostCells(post.getPostId());
        if (cells != null && !cells.isEmpty()) {
            return cells;
        }

        java.util.ArrayList<CommunityPostCell> fallback = new java.util.ArrayList<>();
        int sortOrder = 1;
        if (post.getContent() != null && !post.getContent().isBlank()) {
            fallback.add(new CommunityPostCell(null, post.getPostId(), sortOrder++, CELL_TYPE_TEXT, post.getContent(), null, DEFAULT_ALIGNMENT, DEFAULT_FONT_SIZE_PX, false));
        }
        if (post.getImageUrl() != null && !post.getImageUrl().isBlank()) {
            fallback.add(new CommunityPostCell(null, post.getPostId(), sortOrder, CELL_TYPE_IMAGE, null, post.getImageUrl(), DEFAULT_ALIGNMENT));
        }
        return fallback;
    }

    private String normalizeAlignment(String alignment) {
        String normalized = alignment == null ? DEFAULT_ALIGNMENT : alignment.toUpperCase();
        if (!CELL_ALIGNMENTS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "alignment must be LEFT, CENTER, or RIGHT");
        }
        return normalized;
    }

    private Integer normalizeFontSizePx(Integer fontSizePx) {
        int normalized = fontSizePx == null ? DEFAULT_FONT_SIZE_PX : fontSizePx;
        if (!CELL_FONT_SIZE_PX.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fontSizePx must be 14, 16, 18, 20, 24, 28, or 32");
        }
        return normalized;
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

    private void validateCommentId(Long commentId) {
        if (commentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commentId is required");
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
