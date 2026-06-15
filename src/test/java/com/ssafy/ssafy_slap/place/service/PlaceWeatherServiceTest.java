package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceWeatherServiceTest {

    @Test
    void returnsShortTermForecastsForPlaceCoordinates() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        KmaWeatherClient weatherClient = mock(KmaWeatherClient.class);
        PlaceWeatherService placeWeatherService = new PlaceWeatherService(placeMapper, weatherClient);

        when(placeMapper.findWeatherPointById(1L)).thenReturn(new PlaceWeatherPoint(
                1L,
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));
        when(weatherClient.fetchForecasts(any(), any())).thenReturn(Optional.of(List.of(
                new PlaceWeatherForecast(
                        BigDecimal.valueOf(24.0),
                        60,
                        70,
                        BigDecimal.valueOf(2.0),
                        "RAIN",
                        "CLOUDY",
                        "1mm",
                        LocalDateTime.of(2026, 6, 15, 16, 0),
                        LocalDateTime.of(2026, 6, 15, 14, 0)
                ),
                new PlaceWeatherForecast(
                        BigDecimal.valueOf(23.0),
                        50,
                        75,
                        BigDecimal.valueOf(1.5),
                        "RAIN",
                        "CLOUDY",
                        "1mm",
                        LocalDateTime.of(2026, 6, 15, 17, 0),
                        LocalDateTime.of(2026, 6, 15, 14, 0)
                )
        )));

        var response = placeWeatherService.getWeather(1L, LocalDateTime.of(2026, 6, 15, 15, 20));

        assertThat(response.available()).isTrue();
        assertThat(response.temperature()).isEqualByComparingTo("24.0");
        assertThat(response.feelsLikeTemperature()).isEqualByComparingTo("24.0");
        assertThat(response.precipitationProbability()).isEqualTo(60);
        assertThat(response.humidity()).isEqualTo(70);
        assertThat(response.windSpeed()).isEqualByComparingTo("2.0");
        assertThat(response.precipitationType()).isEqualTo("RAIN");
        assertThat(response.skyStatus()).isEqualTo("CLOUDY");
        assertThat(response.precipitationOneHour()).isEqualTo("1mm");
        assertThat(response.forecastAt()).isEqualTo(LocalDateTime.of(2026, 6, 15, 16, 0));
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 15, 14, 0));
        assertThat(response.forecasts()).hasSize(2);
    }
}
