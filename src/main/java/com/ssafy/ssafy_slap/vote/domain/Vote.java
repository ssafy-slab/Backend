package com.ssafy.ssafy_slap.vote.domain;

import java.time.LocalDateTime;

public class Vote {

    private Long voteId;
    private Long tripId;
    private Long creatorUserId;
    private String title;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public Vote() {
    }

    public Vote(Long voteId, Long tripId, Long creatorUserId, String title, String status,
                LocalDateTime startedAt, LocalDateTime endedAt) {
        this.voteId = voteId;
        this.tripId = tripId;
        this.creatorUserId = creatorUserId;
        this.title = title;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }
    public Long getTripId() { return tripId; }
    public Long getCreatorUserId() { return creatorUserId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
}
