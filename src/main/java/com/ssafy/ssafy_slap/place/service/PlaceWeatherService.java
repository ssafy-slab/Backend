package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceDailyWeatherForecast;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherResponse;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PlaceWeatherService {

    private final PlaceMapper placeMapper;
    private final KmaWeatherClient weatherClient;

    public PlaceWeatherService(PlaceMapper placeMapper, KmaWeatherClient weatherClient) {
        this.placeMapper = placeMapper;
        this.weatherClient = weatherClient;
    }

    public PlaceWeatherResponse getWeather(Long placeId) {
        return getWeather(placeId, LocalDateTime.now());
    }

    PlaceWeatherResponse getWeather(Long placeId, LocalDateTime now) {
        var point = placeMapper.findWeatherPointById(placeId);
        if (point == null) {
            throw new NoSuchElementException("Place not found: " + placeId);
        }
        if (point.latitude() == null || point.longitude() == null) {
            return PlaceWeatherResponse.unavailable("Place coordinates are missing.");
        }

        KmaGridCoordinate coordinate = KmaGridConverter.fromLatLon(point.latitude(), point.longitude());
        return weatherClient.fetchForecasts(coordinate, now)
                .map(this::toResponse)
                .orElseGet(() -> PlaceWeatherResponse.unavailable("Weather forecast is unavailable."));
    }

    private PlaceWeatherResponse toResponse(List<PlaceWeatherForecast> forecasts) {
        PlaceWeatherForecast forecast = forecasts.get(0);
        List<PlaceDailyWeatherForecast> dailyForecasts = toDailyForecasts(forecasts, forecast.forecastAt().toLocalDate());
        return new PlaceWeatherResponse(
                true,
                null,
                forecast.temperature(),
                calculateFeelsLike(forecast.temperature(), forecast.humidity(), forecast.windSpeed()),
                forecast.precipitationProbability(),
                forecast.humidity(),
                forecast.windSpeed(),
                forecast.precipitationType(),
                forecast.skyStatus(),
                forecast.precipitationOneHour(),
                forecast.forecastAt(),
                forecast.updatedAt(),
                forecasts,
                dailyForecasts
        );
    }

    private List<PlaceDailyWeatherForecast> toDailyForecasts(List<PlaceWeatherForecast> forecasts, LocalDate today) {
        LocalDate endDate = today.plusDays(3);
        Map<LocalDate, List<PlaceWeatherForecast>> forecastsByDate = forecasts.stream()
                .filter(forecast -> forecast.forecastAt() != null)
                .filter(forecast -> forecast.forecastAt().toLocalDate().isAfter(today))
                .filter(forecast -> !forecast.forecastAt().toLocalDate().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        forecast -> forecast.forecastAt().toLocalDate(),
                        java.util.TreeMap::new,
                        Collectors.toList()
                ));

        return forecastsByDate.entrySet().stream()
                .limit(3)
                .map(entry -> toDailyForecast(today, entry.getKey(), entry.getValue()))
                .toList();
    }

    private PlaceDailyWeatherForecast toDailyForecast(LocalDate today, LocalDate forecastDate, List<PlaceWeatherForecast> forecasts) {
        PlaceWeatherForecast representative = forecasts.stream()
                .max(Comparator.comparing(
                        forecast -> forecast.precipitationProbability() == null ? -1 : forecast.precipitationProbability()
                ))
                .orElse(forecasts.get(0));

        return new PlaceDailyWeatherForecast(
                forecastDate,
                dayLabel(today, forecastDate),
                minTemperature(forecasts),
                maxTemperature(forecasts),
                maxPrecipitationProbability(forecasts),
                maxHumidity(forecasts),
                maxWindSpeed(forecasts),
                representative.precipitationType(),
                representative.skyStatus(),
                representative.updatedAt()
        );
    }

    private String dayLabel(LocalDate today, LocalDate forecastDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(today, forecastDate);
        if (days == 1) {
            return "내일";
        }
        if (days == 2) {
            return "모레";
        }
        if (days == 3) {
            return "글피";
        }
        return forecastDate.getDayOfWeek().toString();
    }

    private BigDecimal minTemperature(List<PlaceWeatherForecast> forecasts) {
        return forecasts.stream()
                .map(PlaceWeatherForecast::temperature)
                .filter(java.util.Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private BigDecimal maxTemperature(List<PlaceWeatherForecast> forecasts) {
        return forecasts.stream()
                .map(PlaceWeatherForecast::temperature)
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    private Integer maxPrecipitationProbability(List<PlaceWeatherForecast> forecasts) {
        return forecasts.stream()
                .map(PlaceWeatherForecast::precipitationProbability)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private Integer maxHumidity(List<PlaceWeatherForecast> forecasts) {
        return forecasts.stream()
                .map(PlaceWeatherForecast::humidity)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private BigDecimal maxWindSpeed(List<PlaceWeatherForecast> forecasts) {
        return forecasts.stream()
                .map(PlaceWeatherForecast::windSpeed)
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    private BigDecimal calculateFeelsLike(BigDecimal temperature, Integer humidity, BigDecimal windSpeed) {
        if (temperature == null) {
            return null;
        }
        if (humidity == null || windSpeed == null) {
            return temperature.setScale(1, RoundingMode.HALF_UP);
        }

        double temp = temperature.doubleValue();
        double windKmh = Math.max(windSpeed.doubleValue() * 3.6, 0.0);
        double feelsLike = temp;

        if (temp <= 10.0 && windKmh >= 4.8) {
            feelsLike = 13.12 + 0.6215 * temp - 11.37 * Math.pow(windKmh, 0.16) + 0.3965 * temp * Math.pow(windKmh, 0.16);
        } else if (temp >= 27.0 && humidity >= 40) {
            double fahrenheit = temp * 9.0 / 5.0 + 32.0;
            double heatIndexF = -42.379
                    + 2.04901523 * fahrenheit
                    + 10.14333127 * humidity
                    - 0.22475541 * fahrenheit * humidity
                    - 0.00683783 * fahrenheit * fahrenheit
                    - 0.05481717 * humidity * humidity
                    + 0.00122874 * fahrenheit * fahrenheit * humidity
                    + 0.00085282 * fahrenheit * humidity * humidity
                    - 0.00000199 * fahrenheit * fahrenheit * humidity * humidity;
            feelsLike = (heatIndexF - 32.0) * 5.0 / 9.0;
        }

        return BigDecimal.valueOf(feelsLike).setScale(1, RoundingMode.HALF_UP);
    }
}
