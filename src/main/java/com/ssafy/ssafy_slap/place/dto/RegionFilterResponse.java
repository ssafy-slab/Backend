package com.ssafy.ssafy_slap.place.dto;

public record RegionFilterResponse(
        Long regionId,
        String regionName,
        String regionFullName,
        Integer regionLevel,
        Long parentRegionId,
        long placeCount
) {
}
