package com.ssafy.ssafy_slap.place.dto;

import java.util.List;

public record NearbyFacilityGroupResponse(
        String facilityType,
        String categoryGroupCode,
        String label,
        boolean cached,
        List<NearbyFacilityResponse> facilities
) {
}
