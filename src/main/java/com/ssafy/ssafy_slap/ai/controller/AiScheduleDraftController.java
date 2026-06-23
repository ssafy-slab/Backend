package com.ssafy.ssafy_slap.ai.controller;

import com.ssafy.ssafy_slap.ai.dto.AiAnalysisResponse;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.service.AiAnalysisService;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trips/{tripId}/ai/schedule-drafts")
public class AiScheduleDraftController {

    private final AiAnalysisService aiAnalysisService;

    public AiScheduleDraftController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping
    public AiAnalysisResponse createDraft(
            @PathVariable Long tripId,
            Authentication authentication,
            @Valid @RequestBody(required = false) AiScheduleDraftRequest request
    ) {
        return aiAnalysisService.analyzeButton(tripId, currentUserId(authentication), request);
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
