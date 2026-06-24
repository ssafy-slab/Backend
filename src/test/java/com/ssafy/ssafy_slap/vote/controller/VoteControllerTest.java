package com.ssafy.ssafy_slap.vote.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.vote.dto.VoteBallotRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteCreateRequest;
import com.ssafy.ssafy_slap.vote.dto.VoteOptionRequest;
import com.ssafy.ssafy_slap.vote.service.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VoteControllerTest {

    private final VoteService voteService = mock(VoteService.class);
    private final VoteController controller = new VoteController(voteService);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(new AuthenticatedUser(7L, "USER"), null, List.of());

    @Test
    void forwardsAuthenticatedUserToCreateAndBallot() {
        var createRequest = new VoteCreateRequest("메뉴", List.of(
                new VoteOptionRequest(null, "흑돼지", null),
                new VoteOptionRequest(null, "해산물", null)
        ));
        var ballotRequest = new VoteBallotRequest(101L);

        controller.createVote(1L, authentication, createRequest);
        controller.castBallot(1L, 10L, authentication, ballotRequest);

        verify(voteService).createVote(1L, 7L, createRequest);
        verify(voteService).castBallot(1L, 10L, 7L, ballotRequest);
    }

    @Test
    void forwardsAuthenticatedUserToLookupAndClose() {
        controller.getVotes(1L, authentication);
        controller.getVote(1L, 10L, authentication);
        controller.closeVote(1L, 10L, authentication);

        verify(voteService).findVotes(1L, 7L);
        verify(voteService).findVote(1L, 10L, 7L);
        verify(voteService).closeVote(1L, 10L, 7L);
    }

    @Test
    void rejectsAnonymousRequest() {
        assertThatThrownBy(() -> controller.getVotes(1L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
