package com.ssafy.ssafy_slap.ai.controller;

import com.ssafy.ssafy_slap.ai.service.AiSuggestionService;
import com.ssafy.ssafy_slap.ai.service.AiSuggestionVoteService;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiSuggestionVoteControllerTest {

    @Test
    void forwardsAuthenticatedUserToSuggestionVoteCreation() {
        AiSuggestionService suggestionService = mock(AiSuggestionService.class);
        AiSuggestionVoteService voteService = mock(AiSuggestionVoteService.class);
        AiSuggestionController controller = new AiSuggestionController(suggestionService, voteService);
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"), null, List.of()
        );

        controller.createVote(1L, 11L, authentication);

        verify(voteService).createSuggestionVote(1L, 11L, 7L);
    }
}
