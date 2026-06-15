package com.ssafy.ssafy_slap.place.mapper;

import com.ssafy.ssafy_slap.place.dto.PlaceCategoryResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchToken;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.dto.RegionFilterResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaceMapper {

    List<PlaceSummaryResponse> findPlaces(
            @Param("category") String category,
            @Param("regionId") Long regionId,
            @Param("searchTokens") List<PlaceSearchToken> searchTokens,
            @Param("size") int size,
            @Param("offset") int offset
    );

    long countPlaces(
            @Param("category") String category,
            @Param("regionId") Long regionId,
            @Param("searchTokens") List<PlaceSearchToken> searchTokens
    );

    PlaceSummaryResponse findById(@Param("placeId") Long placeId);

    PlaceWeatherPoint findWeatherPointById(@Param("placeId") Long placeId);

    List<PlaceCategoryResponse> findCategories();

    List<RegionFilterResponse> findRegions();
}
