package com.ssafy.ssafy_slap.trip.dto;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TripListResponse(
        Long tripId,
        Long ownerUserId,
        String title,
        String description,
        String tripType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TripMemberResponse> members
) {

    public static TripListResponse from(Trip trip, List<TripMember> members) {
        return new TripListResponse(
                trip.getTripId(),
                trip.getOwnerUserId(),
                trip.getTitle(),
                trip.getDescription(),
                trip.getTripType(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus(),
                trip.getCreatedAt(),
                trip.getUpdatedAt(),
                members.stream().map(TripMemberResponse::from).toList()
        );
    }
}
