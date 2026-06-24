package com.ssafy.ssafy_slap.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoteOptionRequest(
        Long placeId,
        @NotBlank @Size(max = 255) String optionTitle,
        String description
) {
}
