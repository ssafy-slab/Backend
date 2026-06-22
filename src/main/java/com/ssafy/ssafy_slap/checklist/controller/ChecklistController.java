package com.ssafy.ssafy_slap.checklist.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemCreateRequest;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemResponse;
import com.ssafy.ssafy_slap.checklist.service.ChecklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/checklist-items")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChecklistItemResponse createChecklistItem(
            @PathVariable Long tripId,
            Authentication authentication,
            @Valid @RequestBody ChecklistItemCreateRequest request
    ) {
        return checklistService.createChecklistItem(tripId, currentUserId(authentication), request);
    }

    @GetMapping
    public List<ChecklistItemResponse> getChecklistItems(
            @PathVariable Long tripId,
            Authentication authentication
    ) {
        return checklistService.findChecklistItems(tripId, currentUserId(authentication));
    }

    @DeleteMapping("/{checklistItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChecklistItem(
            @PathVariable Long tripId,
            @PathVariable Long checklistItemId,
            Authentication authentication
    ) {
        checklistService.deleteChecklistItem(tripId, checklistItemId, currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
