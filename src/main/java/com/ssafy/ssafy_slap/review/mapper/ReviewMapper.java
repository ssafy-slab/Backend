package com.ssafy.ssafy_slap.review.mapper;

import com.ssafy.ssafy_slap.review.dto.PlaceReviewResponse;
import com.ssafy.ssafy_slap.review.dto.MyPlaceReviewResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ReviewMapper {

    boolean existsPlace(@Param("placeId") Long placeId);

    boolean existsByPlaceAndUser(@Param("placeId") Long placeId, @Param("userId") Long userId);

    void insert(
            @Param("placeId") Long placeId,
            @Param("userId") Long userId,
            @Param("rating") Integer rating,
            @Param("content") String content
    );

    void update(
            @Param("placeId") Long placeId,
            @Param("userId") Long userId,
            @Param("rating") Integer rating,
            @Param("content") String content
    );

    void delete(@Param("placeId") Long placeId, @Param("userId") Long userId);

    List<PlaceReviewResponse> findByPlace(@Param("placeId") Long placeId, @Param("currentUserId") Long currentUserId);

    PlaceReviewResponse findByPlaceAndUser(@Param("placeId") Long placeId, @Param("userId") Long userId);

    BigDecimal averageRating(@Param("placeId") Long placeId);

    long countReviews(@Param("placeId") Long placeId);

    List<MyPlaceReviewResponse> findByUser(
            @Param("userId") Long userId,
            @Param("size") int size,
            @Param("offset") int offset
    );

    long countByUser(@Param("userId") Long userId);
}
