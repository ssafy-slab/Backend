package com.ssafy.ssafy_slap.place.mapper;

import com.ssafy.ssafy_slap.ai.dto.AiPlaceCandidate;
import com.ssafy.ssafy_slap.place.dto.PlaceCategoryResponse;
import com.ssafy.ssafy_slap.place.dto.NearbyFacilityResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchToken;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.dto.RegionFilterResponse;
import com.ssafy.ssafy_slap.place.service.KakaoNearbyFacility;
import com.ssafy.ssafy_slap.place.service.NearbyFacilityType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlaceMapper {

    List<PlaceSummaryResponse> findPlaces(
            @Param("category") String category,
            @Param("regionId") Long regionId,
            @Param("searchTokens") List<PlaceSearchToken> searchTokens,
            @Param("sort") String sort,
            @Param("size") int size,
            @Param("offset") int offset
    );

    long countPlaces(
            @Param("category") String category,
            @Param("regionId") Long regionId,
            @Param("searchTokens") List<PlaceSearchToken> searchTokens
    );

    PlaceSummaryResponse findById(@Param("placeId") Long placeId);

    List<AiPlaceCandidate> findAiPlaceCandidates(@Param("placeName") String placeName);

    PlaceWeatherPoint findWeatherPointById(@Param("placeId") Long placeId);

    List<PlaceCategoryResponse> findCategories();

    List<RegionFilterResponse> findRegions();

    int countValidNearbyFacilityCache(
            @Param("placeId") Long placeId,
            @Param("facilityType") NearbyFacilityType facilityType,
            @Param("searchRadiusM") int searchRadiusM,
            @Param("validAfter") LocalDateTime validAfter
    );

    List<NearbyFacilityResponse> findNearbyFacilities(
            @Param("placeId") Long placeId,
            @Param("facilityType") NearbyFacilityType facilityType,
            @Param("searchRadiusM") int searchRadiusM,
            @Param("limit") int limit
    );

    void upsertFacility(@Param("facility") KakaoNearbyFacility facility);

    Long findFacilityIdByExternalId(
            @Param("sourceProvider") String sourceProvider,
            @Param("externalId") String externalId
    );

    void deleteNearbyFacilities(
            @Param("placeId") Long placeId,
            @Param("facilityType") NearbyFacilityType facilityType,
            @Param("searchRadiusM") int searchRadiusM
    );

    void insertNearbyFacility(
            @Param("placeId") Long placeId,
            @Param("facilityId") Long facilityId,
            @Param("facilityType") NearbyFacilityType facilityType,
            @Param("distanceM") Integer distanceM,
            @Param("searchRadiusM") int searchRadiusM
    );

    void upsertNearbyFacilityCache(
            @Param("placeId") Long placeId,
            @Param("facilityType") NearbyFacilityType facilityType,
            @Param("searchRadiusM") int searchRadiusM,
            @Param("resultCount") int resultCount
    );
}
