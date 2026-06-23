package com.ssafy.ssafy_slap.ai.controller;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.service.AiAnalysisService;
import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiScheduleDraftControllerTest {

    private final AiAnalysisService service = mock(AiAnalysisService.class);
    private final AiScheduleDraftController controller = new AiScheduleDraftController(service);

    @Test
    void forwardsAuthenticatedUserToDraftService() {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );
        var request = new AiScheduleDraftRequest(80, "동선을 줄여줘");

        controller.createDraft(1L, authentication, request);

        verify(service).analyzeButton(1L, 7L, request);
    }

    @Test
    void rejectsAnonymousDraftRequest() {
        assertThatThrownBy(() -> controller.createDraft(
                1L,
                null,
                new AiScheduleDraftRequest(null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
