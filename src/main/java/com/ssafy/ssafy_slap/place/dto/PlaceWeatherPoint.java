package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;

public record PlaceWeatherPoint(
        Long placeId,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
