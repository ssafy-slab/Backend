package com.ssafy.ssafy_slap.vote.service;

import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.dto.VoteBallotRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteCreateRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteOptionRequest;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VoteServiceTest {

    @Test
    void createsVoteWithNormalizedOptionsForEditableTrip() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteService service = new VoteService(mapper);
        var request = new VoteCreateRequest(
                "  저녁 메뉴  ",
                List.of(
                        new VoteOptionRequest(null, "  흑돼지  ", "  첫날 저녁  "),
                        new VoteOptionRequest(33L, "해산물", null)
                )
        );

        when(mapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        doAnswer(invocation -> {
            Vote vote = invocation.getArgument(0);
            vote.setVoteId(10L);
            return null;
        }).when(mapper).insertVote(any(Vote.class));
        when(mapper.findVote(1L, 10L)).thenReturn(openVote());
        when(mapper.findOptionsWithCounts(10L)).thenReturn(List.of(
                new VoteOption(101L, 10L, null, "흑돼지", "첫날 저녁", 0, 0L),
                new VoteOption(102L, 10L, 33L, "해산물", null, 1, 0L)
        ));

        var response = service.createVote(1L, 7L, request);

        assertThat(response.voteId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("저녁 메뉴");
        assertThat(response.options()).extracting(option -> option.optionTitle())
                .containsExactly("흑돼지", "해산물");

        ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
        verify(mapper).insertVote(voteCaptor.capture());
        assertThat(voteCaptor.getValue().getTitle()).isEqualTo("저녁 메뉴");

        ArgumentCaptor<VoteOption> optionCaptor = ArgumentCaptor.forClass(VoteOption.class);
        verify(mapper, org.mockito.Mockito.times(2)).insertOption(optionCaptor.capture());
        assertThat(optionCaptor.getAllValues()).extracting(VoteOption::getSortOrder)
                .containsExactly(0, 1);
    }

    @Test
    void rejectsVoteWithFewerThanTwoOptions() {
        VoteService service = new VoteService(mock(VoteMapper.class));

        assertThatThrownBy(() -> service.createVote(
                1L,
                7L,
                new VoteCreateRequest("메뉴", List.of(new VoteOptionRequest(null, "한 개", null)))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returnsCountsAndCurrentUsersSelection() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteService service = new VoteService(mapper);
        when(mapper.existsAccessibleTrip(1L, 7L)).thenReturn(true);
        when(mapper.findVote(1L, 10L)).thenReturn(openVote());
        when(mapper.findOptionsWithCounts(10L)).thenReturn(List.of(
                new VoteOption(101L, 10L, null, "흑돼지", null, 0, 2L),
                new VoteOption(102L, 10L, null, "해산물", null, 1, 1L)
        ));
        when(mapper.findSelectedOptionId(10L, 7L)).thenReturn(102L);
        when(mapper.countAcceptedTripMembers(1L)).thenReturn(4L);
        when(mapper.countAcceptedMemberBallots(1L, 10L)).thenReturn(3L);

        var response = service.findVote(1L, 10L, 7L);

        assertThat(response.selectedOptionId()).isEqualTo(102L);
        assertThat(response.totalBallotCount()).isEqualTo(3L);
        assertThat(response.options()).extracting(option -> option.voteCount())
                .containsExactly(2L, 1L);
        assertThat(response.eligibleVoterCount()).isEqualTo(4L);
        assertThat(response.votedMemberCount()).isEqualTo(3L);
        assertThat(response.allMembersVoted()).isFalse();
    }

    @Test
    void replacesExistingBallotForOpenVote() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteService service = new VoteService(mapper);
        when(mapper.existsAccessibleTrip(1L, 7L)).thenReturn(true);
        when(mapper.findVote(1L, 10L)).thenReturn(openVote());
        when(mapper.existsOption(10L, 102L)).thenReturn(true);
        when(mapper.findOptionsWithCounts(10L)).thenReturn(List.of(
                new VoteOption(101L, 10L, null, "흑돼지", null, 0, 0L),
                new VoteOption(102L, 10L, null, "해산물", null, 1, 1L)
        ));
        when(mapper.findSelectedOptionId(10L, 7L)).thenReturn(102L);

        var response = service.castBallot(1L, 10L, 7L, new VoteBallotRequest(102L));

        verify(mapper).upsertBallot(10L, 102L, 7L);
        assertThat(response.selectedOptionId()).isEqualTo(102L);
    }

    @Test
    void rejectsOptionFromAnotherVote() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteService service = new VoteService(mapper);
        when(mapper.existsAccessibleTrip(1L, 7L)).thenReturn(true);
        when(mapper.findVote(1L, 10L)).thenReturn(openVote());
        when(mapper.existsOption(10L, 999L)).thenReturn(false);

        assertThatThrownBy(() -> service.castBallot(
                1L,
                10L,
                7L,
                new VoteBallotRequest(999L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsBallotOnClosedVote() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteService service = new VoteService(mapper);
        when(mapper.existsAccessibleTrip(1L, 7L)).thenReturn(true);
        when(mapper.findVote(1L, 10L)).thenReturn(new Vote(
                10L, 1L, 7L, "저녁 메뉴", "CLOSED",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                LocalDateTime.of(2026, 6, 24, 11, 0)
        ));

        assertThatThrownBy(() -> service.castBallot(
                1L,
                10L,
                7L,
                new VoteBallotRequest(101L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private Vote openVote() {
        return new Vote(
                10L,
                1L,
                7L,
                "저녁 메뉴",
                "OPEN",
                LocalDateTime.of(2026, 6, 24, 10, 0),
                null
        );
    }
}
