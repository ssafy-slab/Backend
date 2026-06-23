package com.ssafy.ssafy_slap.ai.controller;

import com.ssafy.ssafy_slap.ai.service.AiSuggestionService;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiSuggestionControllerTest {
    private final AiSuggestionService service = mock(AiSuggestionService.class);
    private final AiSuggestionController controller = new AiSuggestionController(service);
    private final UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(new AuthenticatedUser(7L, "USER"), null, List.of());

    @Test
    void exposesListAndIndividualActions() {
        controller.list(1L, "PENDING", authentication);
        controller.apply(1L, 11L, authentication);
        controller.reject(1L, 12L, authentication);

        verify(service).findSuggestions(1L, 7L, "PENDING");
        verify(service).applySuggestion(1L, 11L, 7L);
        verify(service).rejectSuggestion(1L, 12L, 7L);
    }

    @Test
    void exposesBulkRunActions() {
        controller.applyRun(1L, 5L, authentication);
        controller.rejectRun(1L, 6L, authentication);

        verify(service).applyRun(1L, 5L, 7L);
        verify(service).rejectRun(1L, 6L, 7L);
    }
}
