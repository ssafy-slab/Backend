package com.ssafy.ssafy_slap.review.service;

import com.ssafy.ssafy_slap.review.dto.PlaceReviewRequest;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewResponse;
import com.ssafy.ssafy_slap.review.dto.PlaceReviewSummaryResponse;
import com.ssafy.ssafy_slap.review.dto.MyPlaceReviewResponse;
import com.ssafy.ssafy_slap.review.dto.MyPlaceReviewPageResponse;
import com.ssafy.ssafy_slap.review.mapper.ReviewMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    public PlaceReviewSummaryResponse getReviews(Long placeId, Long currentUserId) {
        ensurePlaceExists(placeId);
        return summary(placeId, currentUserId);
    }

    public MyPlaceReviewPageResponse getMyReviews(Long userId, Integer requestedPage, Integer requestedSize) {
        int page = requestedPage == null || requestedPage < 0 ? 0 : requestedPage;
        int size = requestedSize == null || requestedSize < 1 ? 10 : Math.min(requestedSize, 50);
        long totalElements = reviewMapper.countByUser(userId);
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);
        int offset = page * size;
        return new MyPlaceReviewPageResponse(
                reviewMapper.findByUser(userId, size, offset),
                totalElements,
                page,
                size,
                totalPages
        );
    }

    @Transactional
    public PlaceReviewSummaryResponse create(Long placeId, Long userId, PlaceReviewRequest request) {
        ensureValidRequest(request);
        ensurePlaceExists(placeId);
        if (reviewMapper.existsByPlaceAndUser(placeId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Review already exists");
        }
        reviewMapper.insert(placeId, userId, request.rating(), request.normalizedContent());
        return summary(placeId, userId);
    }

    @Transactional
    public PlaceReviewSummaryResponse update(Long placeId, Long userId, PlaceReviewRequest request) {
        ensureValidRequest(request);
        ensurePlaceExists(placeId);
        ensureOwnReviewExists(placeId, userId);
        reviewMapper.update(placeId, userId, request.rating(), request.normalizedContent());
        return summary(placeId, userId);
    }

    @Transactional
    public PlaceReviewSummaryResponse delete(Long placeId, Long userId) {
        ensurePlaceExists(placeId);
        ensureOwnReviewExists(placeId, userId);
        reviewMapper.delete(placeId, userId);
        return summary(placeId, userId);
    }

    private PlaceReviewSummaryResponse summary(Long placeId, Long currentUserId) {
        BigDecimal average = reviewMapper.averageRating(placeId);
        if (average == null) {
            average = BigDecimal.ZERO;
        }
        PlaceReviewResponse mine = currentUserId == null
                ? null
                : reviewMapper.findByPlaceAndUser(placeId, currentUserId);
        return new PlaceReviewSummaryResponse(
                average,
                reviewMapper.countReviews(placeId),
                reviewMapper.findByPlace(placeId, currentUserId),
                mine
        );
    }

    private void ensurePlaceExists(Long placeId) {
        if (!reviewMapper.existsPlace(placeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
    }

    private void ensureOwnReviewExists(Long placeId, Long userId) {
        if (!reviewMapper.existsByPlaceAndUser(placeId, userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
    }

    private void ensureValidRequest(PlaceReviewRequest request) {
        if (request == null || request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        if (request.content() != null && request.content().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review content is too long");
        }
    }
}
