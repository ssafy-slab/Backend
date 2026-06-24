package com.ssafy.ssafy_slap.vote.dto;

import com.ssafy.ssafy_slap.vote.domain.VoteOption;

public record VoteOptionResponse(
        Long voteOptionId,
        Long placeId,
        String optionTitle,
        String description,
        Integer sortOrder,
        long voteCount
) {
    public static VoteOptionResponse from(VoteOption option) {
        return new VoteOptionResponse(
                option.getVoteOptionId(),
                option.getPlaceId(),
                option.getOptionTitle(),
                option.getDescription(),
                option.getSortOrder(),
                option.getVoteCount() == null ? 0L : option.getVoteCount()
        );
    }
}
