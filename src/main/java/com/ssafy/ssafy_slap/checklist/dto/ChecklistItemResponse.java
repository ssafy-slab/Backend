package com.ssafy.ssafy_slap.checklist.dto;

import com.ssafy.ssafy_slap.checklist.domain.ChecklistItem;

import java.time.LocalDateTime;

public record ChecklistItemResponse(
        Long checklistItemId,
        Long tripId,
        Long assigneeUserId,
        String title,
        boolean done,
        LocalDateTime dueAt,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    public static ChecklistItemResponse from(ChecklistItem item) {
        return new ChecklistItemResponse(
                item.getChecklistItemId(),
                item.getTripId(),
                item.getAssigneeUserId(),
                item.getTitle(),
                item.isDone(),
                item.getDueAt(),
                item.getCreatedAt(),
                item.getCompletedAt()
        );
    }
}
