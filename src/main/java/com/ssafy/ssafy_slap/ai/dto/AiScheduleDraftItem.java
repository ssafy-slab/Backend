package com.ssafy.ssafy_slap.ai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AiScheduleDraftItem(
        String placeName,
        String regionHint,
        LocalDate scheduleDate,
        LocalTime startTime,
        LocalTime endTime,
        String title,
        String memo,
        Integer dayNo,
        Integer sortOrder
) {
}
