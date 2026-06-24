package com.ssafy.ssafy_slap.vote.dto;

import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;

import java.time.LocalDateTime;
import java.util.List;

public record VoteResponse(
        Long voteId,
        Long tripId,
        Long creatorUserId,
        String title,
        String status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<VoteOptionResponse> options,
        Long selectedOptionId,
        long totalBallotCount
) {
    public static VoteResponse of(Vote vote, List<VoteOption> options, Long selectedOptionId) {
        List<VoteOptionResponse> optionResponses = options.stream()
                .map(VoteOptionResponse::from)
                .toList();
        long total = optionResponses.stream().mapToLong(VoteOptionResponse::voteCount).sum();
        return new VoteResponse(
                vote.getVoteId(), vote.getTripId(), vote.getCreatorUserId(), vote.getTitle(),
                vote.getStatus(), vote.getStartedAt(), vote.getEndedAt(),
                optionResponses, selectedOptionId, total
        );
    }
}
