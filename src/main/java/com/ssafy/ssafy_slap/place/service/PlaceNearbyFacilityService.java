package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.NearbyFacilityGroupResponse;
import com.ssafy.ssafy_slap.place.dto.NearbyFacilityResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceNearbyFacilitiesResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PlaceNearbyFacilityService {

    private static final int DEFAULT_RADIUS_M = 1000;
    private static final int MAX_RADIUS_M = 20000;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 15;

    private final PlaceMapper placeMapper;
    private final KakaoLocalClient kakaoLocalClient;
    private final int cacheTtlDays;

    public PlaceNearbyFacilityService(
            PlaceMapper placeMapper,
            KakaoLocalClient kakaoLocalClient,
            @Value("${kakao.local.nearby-cache-ttl-days:7}") int cacheTtlDays
    ) {
        this.placeMapper = placeMapper;
        this.kakaoLocalClient = kakaoLocalClient;
        this.cacheTtlDays = cacheTtlDays;
    }

    @Transactional
    public PlaceNearbyFacilitiesResponse getNearbyFacilities(
            Long placeId,
            Integer radiusM,
            Integer limit,
            String types
    ) {
        int normalizedRadiusM = normalizeRadius(radiusM);
        int normalizedLimit = normalizeLimit(limit);
        List<NearbyFacilityType> facilityTypes = parseTypes(types);

        PlaceWeatherPoint point = placeMapper.findWeatherPointById(placeId);
        if (point == null) {
            throw new NoSuchElementException("Place not found: " + placeId);
        }
        if (point.latitude() == null || point.longitude() == null) {
            return new PlaceNearbyFacilitiesResponse(placeId, normalizedRadiusM, List.of());
        }

        LocalDateTime validAfter = LocalDateTime.now().minusDays(Math.max(cacheTtlDays, 1));
        List<NearbyFacilityGroupResponse> groups = facilityTypes.stream()
                .map(type -> loadGroup(placeId, point, normalizedRadiusM, normalizedLimit, type, validAfter))
                .toList();

        return new PlaceNearbyFacilitiesResponse(placeId, normalizedRadiusM, groups);
    }

    private NearbyFacilityGroupResponse loadGroup(
            Long placeId,
            PlaceWeatherPoint point,
            int radiusM,
            int limit,
            NearbyFacilityType type,
            LocalDateTime validAfter
    ) {
        boolean cacheValid = placeMapper.countValidNearbyFacilityCache(placeId, type, radiusM, validAfter) > 0;
        if (!cacheValid) {
            refreshFacilities(placeId, point, radiusM, limit, type);
        }

        List<NearbyFacilityResponse> facilities = placeMapper.findNearbyFacilities(placeId, type, radiusM, limit);
        return new NearbyFacilityGroupResponse(type.name(), type.categoryGroupCode(), type.label(), cacheValid, facilities);
    }

    private void refreshFacilities(Long placeId, PlaceWeatherPoint point, int radiusM, int limit, NearbyFacilityType type) {
        List<KakaoNearbyFacility> facilities = kakaoLocalClient.searchByCategory(
                type,
                point.latitude(),
                point.longitude(),
                radiusM,
                limit
        );

        placeMapper.deleteNearbyFacilities(placeId, type, radiusM);
        for (KakaoNearbyFacility facility : facilities) {
            placeMapper.upsertFacility(facility);
            Long facilityId = placeMapper.findFacilityIdByExternalId("KAKAO", facility.externalId());
            if (facilityId == null) {
                continue;
            }
            placeMapper.insertNearbyFacility(placeId, facilityId, type, facility.distanceM(), radiusM);
        }
        placeMapper.upsertNearbyFacilityCache(placeId, type, radiusM, facilities.size());
    }

    private int normalizeRadius(Integer radiusM) {
        if (radiusM == null || radiusM <= 0) {
            return DEFAULT_RADIUS_M;
        }
        return Math.min(radiusM, MAX_RADIUS_M);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<NearbyFacilityType> parseTypes(String types) {
        if (!StringUtils.hasText(types)) {
            return NearbyFacilityType.defaultValues();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(NearbyFacilityType::fromName)
                .distinct()
                .toList();
    }
}
