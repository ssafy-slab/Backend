package com.ssafy.ssafy_slap.vote.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VoteCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull @Size(min = 2) List<@Valid VoteOptionRequest> options
) {
}
