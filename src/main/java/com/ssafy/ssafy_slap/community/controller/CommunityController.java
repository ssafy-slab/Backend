package com.ssafy.ssafy_slap.community.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityCommentUpdateRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityImageResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostDetailResponse;
import com.ssafy.ssafy_slap.community.dto.CommunityPostRequest;
import com.ssafy.ssafy_slap.community.dto.CommunityPostSummaryResponse;
import com.ssafy.ssafy_slap.community.service.CommunityImageStorageService;
import com.ssafy.ssafy_slap.community.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final CommunityImageStorageService imageStorageService;

    public CommunityController(
            CommunityService communityService,
            CommunityImageStorageService imageStorageService
    ) {
        this.communityService = communityService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/posts")
    public List<CommunityPostSummaryResponse> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        return communityService.findPosts(category, keyword, sort, page, size, optionalCurrentUserId(authentication));
    }

    @GetMapping("/posts/{postId}")
    public CommunityPostDetailResponse getPost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        return communityService.findPost(postId, optionalCurrentUserId(authentication));
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityPostDetailResponse createPost(
            Authentication authentication,
            @Valid @RequestBody CommunityPostRequest request
    ) {
        return communityService.createPost(currentUserId(authentication), request);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityImageResponse uploadImage(
            Authentication authentication,
            @RequestParam("image") MultipartFile image
    ) {
        currentUserId(authentication);
        return imageStorageService.store(image);
    }

    @PutMapping("/posts/{postId}")
    public CommunityPostDetailResponse updatePost(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommunityPostRequest request
    ) {
        return communityService.updatePost(postId, currentUserId(authentication), request);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        communityService.deletePost(postId, currentUserId(authentication));
    }

    @PostMapping("/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toggleLike(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        communityService.toggleLike(postId, currentUserId(authentication));
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommunityCommentResponse> getComments(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        return communityService.findComments(postId, optionalCurrentUserId(authentication));
    }

    @PostMapping("/posts/{postId}/comments")
    public List<CommunityCommentResponse> createComment(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommunityCommentRequest request
    ) {
        return communityService.createComment(postId, currentUserId(authentication), request);
    }

    @PutMapping("/comments/{commentId}")
    public List<CommunityCommentResponse> updateComment(
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @RequestBody CommunityCommentUpdateRequest request
    ) {
        return communityService.updateComment(commentId, currentUserId(authentication), request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        communityService.deleteComment(commentId, currentUserId(authentication));
    }

    private Long optionalCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.userId();
        }
        return null;
    }

    private Long currentUserId(Authentication authentication) {
        Long userId = optionalCurrentUserId(authentication);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return userId;
    }
}
