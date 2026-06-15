package com.ssafy.ssafy_slap.place.service;

import java.math.BigDecimal;

public record KakaoNearbyFacility(
        String externalId,
        NearbyFacilityType facilityType,
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
