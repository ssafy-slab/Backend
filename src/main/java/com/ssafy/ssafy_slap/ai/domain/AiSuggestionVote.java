package com.ssafy.ssafy_slap.ai.domain;

import java.time.LocalDateTime;

public class AiSuggestionVote {

    private Long aiSuggestionVoteId;
    private Long aiSuggestionId;
    private Long voteId;
    private Long approveOptionId;
    private Long rejectOptionId;
    private String resolution;
    private LocalDateTime resolvedAt;

    public AiSuggestionVote() {
    }

    public AiSuggestionVote(
            Long aiSuggestionVoteId,
            Long aiSuggestionId,
            Long voteId,
            Long approveOptionId,
            Long rejectOptionId,
            String resolution,
            LocalDateTime resolvedAt
    ) {
        this.aiSuggestionVoteId = aiSuggestionVoteId;
        this.aiSuggestionId = aiSuggestionId;
        this.voteId = voteId;
        this.approveOptionId = approveOptionId;
        this.rejectOptionId = rejectOptionId;
        this.resolution = resolution;
        this.resolvedAt = resolvedAt;
    }

    public Long getAiSuggestionVoteId() { return aiSuggestionVoteId; }
    public Long getAiSuggestionId() { return aiSuggestionId; }
    public Long getVoteId() { return voteId; }
    public Long getApproveOptionId() { return approveOptionId; }
    public Long getRejectOptionId() { return rejectOptionId; }
    public String getResolution() { return resolution; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
