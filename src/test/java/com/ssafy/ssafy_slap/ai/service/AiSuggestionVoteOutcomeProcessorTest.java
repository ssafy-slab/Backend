package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestionVote;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiSuggestionVoteOutcomeProcessorTest {

    @Test
    void approvalWinCreatesScheduleAndAppliesSuggestion() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        var processor = new AiSuggestionVoteOutcomeProcessor(suggestionMapper, voteMapper, scheduleMapper);
        stubLinkedVote(suggestionMapper, voteMapper, 2L, 1L);
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion());
        doAnswer(invocation -> {
            TripScheduleItem item = invocation.getArgument(0);
            item.setScheduleItemId(99L);
            return null;
        }).when(scheduleMapper).insertScheduleItem(any(TripScheduleItem.class));
        when(suggestionMapper.markAppliedFromVoting(11L, 99L)).thenReturn(1);
        when(suggestionMapper.markSuggestionVoteResolved(20L, "APPROVED")).thenReturn(1);

        processor.beforeClose(1L, 20L, 7L);

        verify(scheduleMapper).insertScheduleItem(any(TripScheduleItem.class));
        verify(suggestionMapper).markAppliedFromVoting(11L, 99L);
        verify(suggestionMapper).markSuggestionVoteResolved(20L, "APPROVED");
    }

    @Test
    void tieRejectsSuggestionWithoutCreatingSchedule() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        var processor = new AiSuggestionVoteOutcomeProcessor(suggestionMapper, voteMapper, scheduleMapper);
        stubLinkedVote(suggestionMapper, voteMapper, 1L, 1L);
        when(suggestionMapper.markRejectedFromVoting(11L)).thenReturn(1);
        when(suggestionMapper.markSuggestionVoteResolved(20L, "REJECTED")).thenReturn(1);

        processor.beforeClose(1L, 20L, 7L);

        verify(scheduleMapper, never()).insertScheduleItem(any());
        verify(suggestionMapper).markRejectedFromVoting(11L);
        verify(suggestionMapper).markSuggestionVoteResolved(20L, "REJECTED");
    }

    @Test
    void scheduleConflictAbortsVoteClose() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        var processor = new AiSuggestionVoteOutcomeProcessor(suggestionMapper, voteMapper, scheduleMapper);
        stubLinkedVote(suggestionMapper, voteMapper, 2L, 0L);
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion());
        org.mockito.Mockito.doThrow(new DuplicateKeyException("duplicate"))
                .when(scheduleMapper).insertScheduleItem(any(TripScheduleItem.class));

        assertThatThrownBy(() -> processor.beforeClose(1L, 20L, 7L))
                .isInstanceOf(ResponseStatusException.class);

        verify(suggestionMapper, never()).markAppliedFromVoting(any(), any());
    }

    @Test
    void rejectsCloseUntilEveryAcceptedTripMemberHasVoted() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        VoteMapper voteMapper = mock(VoteMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        var processor = new AiSuggestionVoteOutcomeProcessor(suggestionMapper, voteMapper, scheduleMapper);
        stubLinkedVote(suggestionMapper, voteMapper, 1L, 1L);
        when(voteMapper.countAcceptedTripMembers(1L)).thenReturn(3L);
        when(voteMapper.countAcceptedMemberBallots(1L, 20L)).thenReturn(2L);

        assertThatThrownBy(() -> processor.beforeClose(1L, 20L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException statusException = (ResponseStatusException) exception;
                    org.assertj.core.api.Assertions.assertThat(statusException.getStatusCode().value())
                            .isEqualTo(409);
                    org.assertj.core.api.Assertions.assertThat(statusException.getReason())
                            .isEqualTo("All trip members must vote before closing");
                });

        verify(scheduleMapper, never()).insertScheduleItem(any());
        verify(suggestionMapper, never()).markRejectedFromVoting(any());
        verify(suggestionMapper, never()).markSuggestionVoteResolved(any(), any());
    }

    private void stubLinkedVote(
            AiSuggestionMapper suggestionMapper,
            VoteMapper voteMapper,
            long approveCount,
            long rejectCount
    ) {
        when(suggestionMapper.findSuggestionVoteByVoteIdForUpdate(20L))
                .thenReturn(new AiSuggestionVote(1L, 11L, 20L, 201L, 202L, null, null));
        when(voteMapper.findOptionsWithCounts(20L)).thenReturn(List.of(
                new VoteOption(201L, 20L, null, "찬성", null, 0, approveCount),
                new VoteOption(202L, 20L, null, "반대", null, 1, rejectCount)
        ));
        when(voteMapper.countAcceptedTripMembers(1L)).thenReturn(approveCount + rejectCount);
        when(voteMapper.countAcceptedMemberBallots(1L, 20L)).thenReturn(approveCount + rejectCount);
    }

    private AiSuggestion suggestion() {
        return new AiSuggestion(
                11L, 5L, 1L, 351L, "해운대해수욕장", "부산 해운대구",
                "SCHEDULE", "해운대 방문", "오전 관광", "채팅 합의",
                LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), LocalTime.of(12, 0),
                1, 1, "VOTING", null, null, null
        );
    }
}
