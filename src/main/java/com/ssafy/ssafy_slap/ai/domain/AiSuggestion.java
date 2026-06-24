package com.ssafy.ssafy_slap.ai.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AiSuggestion {
    private Long aiSuggestionId;
    private Long analysisRunId;
    private Long tripId;
    private Long suggestedPlaceId;
    private String suggestedPlaceName;
    private String suggestedRegionHint;
    private String suggestionType;
    private String suggestedTitle;
    private String summary;
    private String reason;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer dayNo;
    private Integer sortOrder;
    private String status;
    private Long appliedScheduleItemId;
    private Long voteId;
    private LocalDateTime createdAt;
    private LocalDateTime appliedAt;

    public AiSuggestion() {
    }

    public AiSuggestion(Long aiSuggestionId, Long analysisRunId, Long tripId, Long suggestedPlaceId,
                        String suggestedPlaceName, String suggestedRegionHint,
                        String suggestionType, String suggestedTitle, String summary, String reason,
                        LocalDate scheduleDate, LocalTime startTime, LocalTime endTime,
                        Integer dayNo, Integer sortOrder, String status, Long appliedScheduleItemId,
                        LocalDateTime createdAt, LocalDateTime appliedAt) {
        this.aiSuggestionId = aiSuggestionId;
        this.analysisRunId = analysisRunId;
        this.tripId = tripId;
        this.suggestedPlaceId = suggestedPlaceId;
        this.suggestedPlaceName = suggestedPlaceName;
        this.suggestedRegionHint = suggestedRegionHint;
        this.suggestionType = suggestionType;
        this.suggestedTitle = suggestedTitle;
        this.summary = summary;
        this.reason = reason;
        this.scheduleDate = scheduleDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayNo = dayNo;
        this.sortOrder = sortOrder;
        this.status = status;
        this.appliedScheduleItemId = appliedScheduleItemId;
        this.createdAt = createdAt;
        this.appliedAt = appliedAt;
    }

    public Long getAiSuggestionId() { return aiSuggestionId; }
    public void setAiSuggestionId(Long value) { this.aiSuggestionId = value; }
    public Long getAnalysisRunId() { return analysisRunId; }
    public void setAnalysisRunId(Long value) { this.analysisRunId = value; }
    public Long getTripId() { return tripId; }
    public Long getSuggestedPlaceId() { return suggestedPlaceId; }
    public String getSuggestedPlaceName() { return suggestedPlaceName; }
    public String getSuggestedRegionHint() { return suggestedRegionHint; }
    public String getSuggestionType() { return suggestionType; }
    public String getSuggestedTitle() { return suggestedTitle; }
    public String getSummary() { return summary; }
    public String getReason() { return reason; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Integer getDayNo() { return dayNo; }
    public Integer getSortOrder() { return sortOrder; }
    public String getStatus() { return status; }
    public Long getAppliedScheduleItemId() { return appliedScheduleItemId; }
    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
}
