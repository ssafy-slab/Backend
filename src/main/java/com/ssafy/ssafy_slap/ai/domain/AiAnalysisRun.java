package com.ssafy.ssafy_slap.ai.domain;

import java.time.LocalDateTime;

public class AiAnalysisRun {
    private Long analysisRunId;
    private Long tripId;
    private Long requestedByUserId;
    private String triggerType;
    private Long firstMessageId;
    private Long lastMessageId;
    private Integer messageCount;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public AiAnalysisRun() {
    }

    public AiAnalysisRun(Long analysisRunId, Long tripId, Long requestedByUserId, String triggerType,
                         Long firstMessageId, Long lastMessageId, Integer messageCount, String status,
                         String errorMessage, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.analysisRunId = analysisRunId;
        this.tripId = tripId;
        this.requestedByUserId = requestedByUserId;
        this.triggerType = triggerType;
        this.firstMessageId = firstMessageId;
        this.lastMessageId = lastMessageId;
        this.messageCount = messageCount;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getAnalysisRunId() { return analysisRunId; }
    public void setAnalysisRunId(Long value) { this.analysisRunId = value; }
    public Long getTripId() { return tripId; }
    public Long getRequestedByUserId() { return requestedByUserId; }
    public String getTriggerType() { return triggerType; }
    public Long getFirstMessageId() { return firstMessageId; }
    public Long getLastMessageId() { return lastMessageId; }
    public Integer getMessageCount() { return messageCount; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
