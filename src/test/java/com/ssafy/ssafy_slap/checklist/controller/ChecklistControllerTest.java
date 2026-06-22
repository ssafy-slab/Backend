package com.ssafy.ssafy_slap.checklist.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemCreateRequest;
import com.ssafy.ssafy_slap.checklist.service.ChecklistService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChecklistControllerTest {

    private final ChecklistService checklistService = mock(ChecklistService.class);
    private final ChecklistController controller = new ChecklistController(checklistService);

    @Test
    void passesAuthenticatedUserToCreate() {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );
        var request = new ChecklistItemCreateRequest("pack passport", null, null);

        controller.createChecklistItem(1L, authentication, request);

        verify(checklistService).createChecklistItem(1L, 7L, request);
    }

    @Test
    void passesAuthenticatedUserToLookup() {
        var authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(7L, "USER"),
                null,
                List.of()
        );

        controller.getChecklistItems(1L, authentication);

        verify(checklistService).findChecklistItems(1L, 7L);
    }

    @Test
    void rejectsAnonymousMutation() {
        assertThatThrownBy(() -> controller.deleteChecklistItem(1L, 99L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
