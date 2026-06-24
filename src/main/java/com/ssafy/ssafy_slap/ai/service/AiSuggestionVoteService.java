package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.dto.VoteResponse;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiSuggestionVoteService {

    private final AiSuggestionMapper suggestionMapper;
    private final VoteMapper voteMapper;

    public AiSuggestionVoteService(AiSuggestionMapper suggestionMapper, VoteMapper voteMapper) {
        this.suggestionMapper = suggestionMapper;
        this.voteMapper = voteMapper;
    }

    @Transactional
    public VoteResponse createSuggestionVote(Long tripId, Long suggestionId, Long userId) {
        if (!voteMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }
        if (!"TEAM".equalsIgnoreCase(suggestionMapper.findTripType(tripId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion voting is only for team trips");
        }
        AiSuggestion suggestion = suggestionMapper.findSuggestionForUpdate(tripId, suggestionId);
        if (suggestion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI suggestion not found");
        }
        if (!"PENDING".equals(suggestion.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is not pending");
        }
        if (suggestionMapper.findSuggestionVote(suggestionId) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion vote already exists");
        }

        Vote vote = new Vote(
                null, tripId, userId, "AI 제안: " + suggestion.getSuggestedTitle(),
                "OPEN", null, null
        );
        voteMapper.insertVote(vote);

        VoteOption approve = new VoteOption(
                null, vote.getVoteId(), suggestion.getSuggestedPlaceId(), "찬성",
                "AI 제안을 일정에 추가합니다.", 0, 0L
        );
        VoteOption reject = new VoteOption(
                null, vote.getVoteId(), suggestion.getSuggestedPlaceId(), "반대",
                "AI 제안을 일정에 추가하지 않습니다.", 1, 0L
        );
        voteMapper.insertOption(approve);
        voteMapper.insertOption(reject);
        suggestionMapper.insertSuggestionVote(
                suggestionId, vote.getVoteId(), approve.getVoteOptionId(), reject.getVoteOptionId()
        );
        if (suggestionMapper.markVoting(suggestionId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is no longer pending");
        }
        return VoteResponse.of(
                voteMapper.findVote(tripId, vote.getVoteId()),
                voteMapper.findOptionsWithCounts(vote.getVoteId()),
                voteMapper.findSelectedOptionId(vote.getVoteId(), userId),
                voteMapper.countAcceptedTripMembers(tripId),
                voteMapper.countAcceptedMemberBallots(tripId, vote.getVoteId())
        );
    }
}
