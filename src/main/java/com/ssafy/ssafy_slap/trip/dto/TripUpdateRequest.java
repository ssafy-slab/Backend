package com.ssafy.ssafy_slap.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TripUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @Size(max = 50) String tripType,
        LocalDate startDate,
        LocalDate endDate
) {
}
