package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherResponse;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

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
            return PlaceWeatherResponse.unavailable("좌표가 없어 날씨 정보를 불러올 수 없습니다.");
        }

        KmaGridCoordinate coordinate = KmaGridConverter.fromLatLon(point.latitude(), point.longitude());
        return weatherClient.fetchForecast(coordinate, now)
                .map(this::toResponse)
                .orElseGet(() -> PlaceWeatherResponse.unavailable("날씨 정보를 불러올 수 없습니다."));
    }

    private PlaceWeatherResponse toResponse(PlaceWeatherForecast forecast) {
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
                forecast.updatedAt()
        );
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
