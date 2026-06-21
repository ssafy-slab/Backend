package com.ssafy.ssafy_slap.trip.dto;

import com.ssafy.ssafy_slap.trip.domain.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripResponse(
        Long tripId,
        Long ownerUserId,
        String title,
        String description,
        String tripType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getTripId(),
                trip.getOwnerUserId(),
                trip.getTitle(),
                trip.getDescription(),
                trip.getTripType(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
