package com.ssafy.ssafy_slap.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ChecklistItemCreateRequest(
        @NotBlank @Size(max = 255) String title,
        Long assigneeUserId,
        LocalDateTime dueAt
) {
}
