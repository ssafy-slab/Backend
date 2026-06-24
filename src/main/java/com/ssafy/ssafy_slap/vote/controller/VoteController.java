package com.ssafy.ssafy_slap.vote.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.vote.dto.VoteBallotRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteCreateRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteResponse;
import com.ssafy.ssafy_slap.vote.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse createVote(
            @PathVariable Long tripId,
            Authentication authentication,
            @Valid @RequestBody VoteCreateRequest request
    ) {
        return voteService.createVote(tripId, currentUserId(authentication), request);
    }

    @GetMapping
    public List<VoteResponse> getVotes(
            @PathVariable Long tripId,
            Authentication authentication
    ) {
        return voteService.findVotes(tripId, currentUserId(authentication));
    }

    @GetMapping("/{voteId}")
    public VoteResponse getVote(
            @PathVariable Long tripId,
            @PathVariable Long voteId,
            Authentication authentication
    ) {
        return voteService.findVote(tripId, voteId, currentUserId(authentication));
    }

    @PutMapping("/{voteId}/ballot")
    public VoteResponse castBallot(
            @PathVariable Long tripId,
            @PathVariable Long voteId,
            Authentication authentication,
            @Valid @RequestBody VoteBallotRequest request
    ) {
        return voteService.castBallot(tripId, voteId, currentUserId(authentication), request);
    }

    @PatchMapping("/{voteId}/close")
    public VoteResponse closeVote(
            @PathVariable Long tripId,
            @PathVariable Long voteId,
            Authentication authentication
    ) {
        return voteService.closeVote(tripId, voteId, currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
