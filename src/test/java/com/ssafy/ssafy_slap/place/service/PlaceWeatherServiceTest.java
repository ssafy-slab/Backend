package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceWeatherServiceTest {

    @Test
    void returnsWeatherForPlaceCoordinates() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        KmaWeatherClient weatherClient = mock(KmaWeatherClient.class);
        PlaceWeatherService placeWeatherService = new PlaceWeatherService(placeMapper, weatherClient);

        when(placeMapper.findWeatherPointById(1L)).thenReturn(new PlaceWeatherPoint(
                1L,
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));
        when(weatherClient.fetchForecast(any(), any())).thenReturn(Optional.of(new PlaceWeatherForecast(
                BigDecimal.valueOf(24.0),
                60,
                70,
                BigDecimal.valueOf(2.0),
                "비",
                "흐림",
                "1mm",
                LocalDateTime.of(2026, 6, 15, 15, 0)
        )));

        var response = placeWeatherService.getWeather(1L, LocalDateTime.of(2026, 6, 15, 15, 20));

        assertThat(response.available()).isTrue();
        assertThat(response.temperature()).isEqualByComparingTo("24.0");
        assertThat(response.feelsLikeTemperature()).isEqualByComparingTo("24.0");
        assertThat(response.precipitationProbability()).isEqualTo(60);
        assertThat(response.humidity()).isEqualTo(70);
        assertThat(response.windSpeed()).isEqualByComparingTo("2.0");
        assertThat(response.precipitationType()).isEqualTo("비");
        assertThat(response.skyStatus()).isEqualTo("흐림");
        assertThat(response.precipitationOneHour()).isEqualTo("1mm");
    }
}
