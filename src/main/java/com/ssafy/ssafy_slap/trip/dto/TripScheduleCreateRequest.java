package com.ssafy.ssafy_slap.trip.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record TripScheduleCreateRequest(
        @NotNull Long placeId,
        @NotNull LocalDate scheduleDate,
        @NotNull LocalTime startTime,
        LocalTime endTime,
        @Size(max = 255) String title,
        String memo,
        Integer dayNo,
        Integer sortOrder
) {
}
