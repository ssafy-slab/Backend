package com.ssafy.ssafy_slap.place.dto;

import java.util.List;

public record PlaceNearbyFacilitiesResponse(
        Long placeId,
        Integer searchRadiusM,
        List<NearbyFacilityGroupResponse> groups
) {
}
