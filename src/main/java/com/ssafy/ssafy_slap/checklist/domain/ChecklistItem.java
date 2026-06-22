package com.ssafy.ssafy_slap.checklist.domain;

import java.time.LocalDateTime;

public class ChecklistItem {

    private Long checklistItemId;
    private Long tripId;
    private Long assigneeUserId;
    private String title;
    private boolean done;
    private LocalDateTime dueAt;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ChecklistItem() {
    }

    public ChecklistItem(
            Long checklistItemId,
            Long tripId,
            Long assigneeUserId,
            String title,
            boolean done,
            LocalDateTime dueAt,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        this.checklistItemId = checklistItemId;
        this.tripId = tripId;
        this.assigneeUserId = assigneeUserId;
        this.title = title;
        this.done = done;
        this.dueAt = dueAt;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getChecklistItemId() {
        return checklistItemId;
    }

    public void setChecklistItemId(Long checklistItemId) {
        this.checklistItemId = checklistItemId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
