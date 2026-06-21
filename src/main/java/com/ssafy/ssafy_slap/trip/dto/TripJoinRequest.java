package com.ssafy.ssafy_slap.trip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TripJoinRequest(
        @NotBlank @Size(max = 20) String inviteCode
) {
}
