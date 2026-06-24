package com.ssafy.ssafy_slap.vote.dto;

import jakarta.validation.constraints.NotNull;

public record VoteBallotRequest(@NotNull Long voteOptionId) {
}
