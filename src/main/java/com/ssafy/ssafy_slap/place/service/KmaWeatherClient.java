package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;

import java.time.LocalDateTime;
import java.util.Optional;

public interface KmaWeatherClient {

    Optional<PlaceWeatherForecast> fetchForecast(KmaGridCoordinate coordinate, LocalDateTime now);
}
