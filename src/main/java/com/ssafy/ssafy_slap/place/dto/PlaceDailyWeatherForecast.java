package com.ssafy.ssafy_slap.place.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PlaceDailyWeatherForecast(
        LocalDate forecastDate,
        String dayLabel,
        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        Integer precipitationProbability,
        Integer humidity,
        BigDecimal windSpeed,
        String precipitationType,
        String skyStatus,
        LocalDateTime updatedAt
) {
}
