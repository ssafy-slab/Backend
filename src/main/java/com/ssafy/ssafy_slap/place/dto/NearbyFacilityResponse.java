package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;

public record NearbyFacilityResponse(
        Long facilityId,
        String facilityType,
        String categoryGroupCode,
        String categoryName,
        String facilityName,
        String phone,
        String address,
        String roadAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String placeUrl,
        Integer distanceM
) {
}
