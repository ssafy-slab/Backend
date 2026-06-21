package com.ssafy.ssafy_slap.trip.dto;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TripScheduleResponse(
        Long scheduleItemId,
        Long tripId,
        Long placeId,
        Long createdByUserId,
        Integer dayNo,
        LocalDate scheduleDate,
        LocalTime startTime,
        LocalTime endTime,
        String title,
        String memo,
        Integer sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TripScheduleResponse from(TripScheduleItem item) {
        return new TripScheduleResponse(
                item.getScheduleItemId(),
                item.getTripId(),
                item.getPlaceId(),
                item.getCreatedByUserId(),
                item.getDayNo(),
                item.getScheduleDate(),
                item.getStartTime(),
                item.getEndTime(),
                item.getTitle(),
                item.getMemo(),
                item.getSortOrder(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
