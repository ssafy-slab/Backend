package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceWeatherResponse(
        boolean available,
        String message,
        BigDecimal temperature,
        BigDecimal feelsLikeTemperature,
        Integer precipitationProbability,
        Integer humidity,
        BigDecimal windSpeed,
        String precipitationType,
        String skyStatus,
        String precipitationOneHour,
        LocalDateTime updatedAt
) {
    public static PlaceWeatherResponse unavailable(String message) {
        return new PlaceWeatherResponse(false, message, null, null, null, null, null, null, null, null, null);
    }
}
