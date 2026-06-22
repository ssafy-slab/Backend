package com.ssafy.ssafy_slap.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TripMemberRoleUpdateRequest(
        @NotBlank @Size(max = 50) String memberRole
) {
}
