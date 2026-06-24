package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiSuggestionVoteServiceTest {

    @Test
    void createsApproveRejectVoteForPendingTeamSuggestion() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        AiSuggestionVoteService service = new AiSuggestionVoteService(suggestionMapper, voteMapper);

        when(voteMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findTripType(1L)).thenReturn("TEAM");
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion("PENDING"));
        when(suggestionMapper.findSuggestionVote(11L)).thenReturn(null);
        doAnswer(invocation -> {
            Vote vote = invocation.getArgument(0);
            vote.setVoteId(20L);
            return null;
        }).when(voteMapper).insertVote(any(Vote.class));
        doAnswer(invocation -> {
            VoteOption option = invocation.getArgument(0);
            option.setVoteOptionId(option.getSortOrder() == 0 ? 201L : 202L);
            return null;
        }).when(voteMapper).insertOption(any(VoteOption.class));
        when(suggestionMapper.markVoting(11L)).thenReturn(1);
        when(voteMapper.findVote(1L, 20L)).thenReturn(
                new Vote(20L, 1L, 7L, "AI 제안: 해운대 방문", "OPEN", null, null)
        );
        when(voteMapper.findOptionsWithCounts(20L)).thenReturn(List.of(
                new VoteOption(201L, 20L, null, "찬성", "AI 제안을 일정에 추가합니다.", 0, 0L),
                new VoteOption(202L, 20L, null, "반대", "AI 제안을 일정에 추가하지 않습니다.", 1, 0L)
        ));

        var response = service.createSuggestionVote(1L, 11L, 7L);

        assertThat(response.voteId()).isEqualTo(20L);
        assertThat(response.options()).extracting(option -> option.optionTitle())
                .containsExactly("찬성", "반대");
        verify(suggestionMapper).insertSuggestionVote(11L, 20L, 201L, 202L);
        verify(suggestionMapper).markVoting(11L);
    }

    @Test
    void rejectsSuggestionVoteForPersonalTrip() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        AiSuggestionVoteService service = new AiSuggestionVoteService(suggestionMapper, voteMapper);

        when(voteMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findTripType(1L)).thenReturn("PERSONAL");

        assertThatThrownBy(() -> service.createSuggestionVote(1L, 11L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsDuplicateSuggestionVote() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        AiSuggestionVoteService service = new AiSuggestionVoteService(suggestionMapper, voteMapper);

        when(voteMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findTripType(1L)).thenReturn("TEAM");
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion("PENDING"));
        when(suggestionMapper.findSuggestionVote(11L))
                .thenReturn(new com.ssafy.ssafy_slap.ai.domain.AiSuggestionVote(
                        1L, 11L, 20L, 201L, 202L, null, null
                ));

        assertThatThrownBy(() -> service.createSuggestionVote(1L, 11L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private AiSuggestion suggestion(String status) {
        return new AiSuggestion(
                11L, 5L, 1L, 351L, "해운대해수욕장", "부산 해운대구",
                "SCHEDULE", "해운대 방문", "오전 관광", "채팅 합의",
                LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), LocalTime.of(12, 0),
                1, 1, status, null, null, null
        );
    }
}
