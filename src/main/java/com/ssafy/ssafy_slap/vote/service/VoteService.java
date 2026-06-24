package com.ssafy.ssafy_slap.vote.service;

import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.dto.VoteBallotRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteCreateRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteOptionRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteResponse;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VoteService {

    private final VoteMapper voteMapper;
    private final List<VoteCloseProcessor> closeProcessors;

    public VoteService(VoteMapper voteMapper) {
        this(voteMapper, List.of());
    }

    @Autowired
    public VoteService(VoteMapper voteMapper, List<VoteCloseProcessor> closeProcessors) {
        this.voteMapper = voteMapper;
        this.closeProcessors = closeProcessors;
    }

    @Transactional
    public VoteResponse createVote(Long tripId, Long userId, VoteCreateRequest request) {
        validateIds(tripId, userId);
        if (request == null || request.options() == null || request.options().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least two options are required");
        }
        String title = requiredText(request.title(), "title");
        if (!voteMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }

        List<NormalizedOption> options = request.options().stream()
                .map(this::normalizeOption)
                .toList();
        Vote vote = new Vote(null, tripId, userId, title, "OPEN", null, null);
        voteMapper.insertVote(vote);
        for (int index = 0; index < options.size(); index++) {
            NormalizedOption option = options.get(index);
            voteMapper.insertOption(new VoteOption(
                    null, vote.getVoteId(), option.placeId(), option.title(),
                    option.description(), index, 0L
            ));
        }
        return loadVote(tripId, vote.getVoteId(), userId);
    }

    @Transactional(readOnly = true)
    public List<VoteResponse> findVotes(Long tripId, Long userId) {
        validateAccess(tripId, userId);
        return voteMapper.findVotesByTripId(tripId).stream()
                .map(vote -> toResponse(vote, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public VoteResponse findVote(Long tripId, Long voteId, Long userId) {
        validateAccess(tripId, userId);
        return loadVote(tripId, voteId, userId);
    }

    @Transactional
    public VoteResponse castBallot(Long tripId, Long voteId, Long userId, VoteBallotRequest request) {
        validateAccess(tripId, userId);
        if (request == null || request.voteOptionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voteOptionId is required");
        }
        Vote vote = requireVote(tripId, voteId);
        if (!"OPEN".equals(vote.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vote is closed");
        }
        if (!voteMapper.existsOption(voteId, request.voteOptionId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option does not belong to vote");
        }
        voteMapper.upsertBallot(voteId, request.voteOptionId(), userId);
        return toResponse(vote, userId);
    }

    @Transactional
    public VoteResponse closeVote(Long tripId, Long voteId, Long userId) {
        validateIds(tripId, userId);
        if (!voteMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }
        Vote vote = requireVote(tripId, voteId);
        if (!"OPEN".equals(vote.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vote is already closed");
        }
        closeProcessors.forEach(processor -> processor.beforeClose(tripId, voteId, userId));
        if (voteMapper.closeVote(tripId, voteId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vote could not be closed");
        }
        return loadVote(tripId, voteId, userId);
    }

    private VoteResponse loadVote(Long tripId, Long voteId, Long userId) {
        return toResponse(requireVote(tripId, voteId), userId);
    }

    private Vote requireVote(Long tripId, Long voteId) {
        if (voteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voteId is required");
        }
        Vote vote = voteMapper.findVote(tripId, voteId);
        if (vote == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vote not found");
        }
        return vote;
    }

    private VoteResponse toResponse(Vote vote, Long userId) {
        return VoteResponse.of(
                vote,
                voteMapper.findOptionsWithCounts(vote.getVoteId()),
                voteMapper.findSelectedOptionId(vote.getVoteId(), userId)
        );
    }

    private void validateAccess(Long tripId, Long userId) {
        validateIds(tripId, userId);
        if (!voteMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not accessible");
        }
    }

    private void validateIds(Long tripId, Long userId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }

    private NormalizedOption normalizeOption(VoteOptionRequest option) {
        if (option == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option is required");
        }
        return new NormalizedOption(
                option.placeId(),
                requiredText(option.optionTitle(), "optionTitle"),
                optionalText(option.description())
        );
    }

    private String requiredText(String text, String fieldName) {
        String normalized = optionalText(text);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return normalized;
    }

    private String optionalText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record NormalizedOption(Long placeId, String title, String description) {
    }
}
