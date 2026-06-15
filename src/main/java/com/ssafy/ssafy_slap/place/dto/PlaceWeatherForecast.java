package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceWeatherForecast(
        BigDecimal temperature,
        Integer precipitationProbability,
        Integer humidity,
        BigDecimal windSpeed,
        String precipitationType,
        String skyStatus,
        String precipitationOneHour,
        LocalDateTime forecastAt,
        LocalDateTime updatedAt
) {
}
