package com.ssafy.ssafy_slap.checklist.service;

import com.ssafy.ssafy_slap.checklist.domain.ChecklistItem;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemCreateRequest;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemResponse;
import com.ssafy.ssafy_slap.checklist.mapper.ChecklistMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChecklistService {

    private final ChecklistMapper checklistMapper;

    public ChecklistService(ChecklistMapper checklistMapper) {
        this.checklistMapper = checklistMapper;
    }

    @Transactional
    public ChecklistItemResponse createChecklistItem(
            Long tripId,
            Long userId,
            ChecklistItemCreateRequest request
    ) {
        String title = validateCreateRequest(tripId, userId, request);

        ChecklistItem item = new ChecklistItem(
                null,
                tripId,
                request.assigneeUserId(),
                title,
                false,
                request.dueAt(),
                null,
                null
        );

        checklistMapper.insertChecklistItem(item);
        return ChecklistItemResponse.from(checklistMapper.findChecklistItemById(item.getChecklistItemId()));
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> findChecklistItems(Long tripId, Long userId) {
        validateTripAccess(tripId, userId);
        return checklistMapper.findChecklistItemsByTripId(tripId).stream()
                .map(ChecklistItemResponse::from)
                .toList();
    }

    @Transactional
    public void deleteChecklistItem(Long tripId, Long checklistItemId, Long userId) {
        validateTripEditAccess(tripId, userId);
        int deleted = checklistMapper.deleteChecklistItem(tripId, checklistItemId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist item not found");
        }
    }

    private String validateCreateRequest(Long tripId, Long userId, ChecklistItemCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        String title = normalizeRequiredText(request.title(), "title");
        validateTripEditAccess(tripId, userId);
        if (request.assigneeUserId() != null
                && !checklistMapper.existsTripMember(tripId, request.assigneeUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assigneeUserId must be a trip member");
        }
        return title;
    }

    private void validateTripAccess(Long tripId, Long userId) {
        validateRequiredIds(tripId, userId);
        if (!checklistMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not accessible");
        }
    }

    private void validateTripEditAccess(Long tripId, Long userId) {
        validateRequiredIds(tripId, userId);
        if (!checklistMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }
    }

    private void validateRequiredIds(Long tripId, Long userId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }

    private String normalizeRequiredText(String text, String fieldName) {
        if (text == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return normalized;
    }
}
