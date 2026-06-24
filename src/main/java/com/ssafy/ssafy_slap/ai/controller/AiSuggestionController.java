package com.ssafy.ssafy_slap.ai.controller;

import com.ssafy.ssafy_slap.ai.dto.AiSuggestionResponse;
import com.ssafy.ssafy_slap.ai.service.AiSuggestionService;
import com.ssafy.ssafy_slap.ai.service.AiSuggestionVoteService;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.vote.dto.VoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/ai")
public class AiSuggestionController {
    private final AiSuggestionService service;
    private final AiSuggestionVoteService voteService;

    public AiSuggestionController(AiSuggestionService service) {
        this(service, null);
    }

    @Autowired
    public AiSuggestionController(AiSuggestionService service, AiSuggestionVoteService voteService) {
        this.service = service;
        this.voteService = voteService;
    }

    @GetMapping("/suggestions")
    public List<AiSuggestionResponse> list(@PathVariable Long tripId,
                                           @RequestParam(required = false) String status,
                                           Authentication authentication) {
        return service.findSuggestions(tripId, currentUserId(authentication), status);
    }

    @PostMapping("/suggestions/{suggestionId}/apply")
    public AiSuggestionResponse apply(@PathVariable Long tripId, @PathVariable Long suggestionId,
                                      Authentication authentication) {
        return service.applySuggestion(tripId, suggestionId, currentUserId(authentication));
    }

    @PatchMapping("/suggestions/{suggestionId}/reject")
    public AiSuggestionResponse reject(@PathVariable Long tripId, @PathVariable Long suggestionId,
                                       Authentication authentication) {
        return service.rejectSuggestion(tripId, suggestionId, currentUserId(authentication));
    }

    @PostMapping("/suggestions/{suggestionId}/vote")
    public VoteResponse createVote(
            @PathVariable Long tripId,
            @PathVariable Long suggestionId,
            Authentication authentication
    ) {
        return voteService.createSuggestionVote(tripId, suggestionId, currentUserId(authentication));
    }

    @PostMapping("/analysis-runs/{runId}/apply")
    public List<AiSuggestionResponse> applyRun(@PathVariable Long tripId, @PathVariable Long runId,
                                               Authentication authentication) {
        return service.applyRun(tripId, runId, currentUserId(authentication));
    }

    @PatchMapping("/analysis-runs/{runId}/reject")
    public List<AiSuggestionResponse> rejectRun(@PathVariable Long tripId, @PathVariable Long runId,
                                                Authentication authentication) {
        return service.rejectRun(tripId, runId, currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
