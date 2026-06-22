package com.ssafy.ssafy_slap.checklist.service;

import com.ssafy.ssafy_slap.checklist.domain.ChecklistItem;
import com.ssafy.ssafy_slap.checklist.dto.ChecklistItemCreateRequest;
import com.ssafy.ssafy_slap.checklist.mapper.ChecklistMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistServiceTest {

    @Test
    void createsChecklistItemForEditableTrip() {
        ChecklistMapper mapper = mock(ChecklistMapper.class);
        ChecklistService service = new ChecklistService(mapper);
        ChecklistItemCreateRequest request = new ChecklistItemCreateRequest(
                "  pack passport  ",
                20L,
                LocalDateTime.of(2026, 7, 1, 9, 0)
        );

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(true);
        when(mapper.existsTripMember(1L, 20L)).thenReturn(true);
        doAnswer(invocation -> {
            ChecklistItem item = invocation.getArgument(0);
            item.setChecklistItemId(99L);
            return null;
        }).when(mapper).insertChecklistItem(org.mockito.ArgumentMatchers.any(ChecklistItem.class));
        when(mapper.findChecklistItemById(99L)).thenReturn(new ChecklistItem(
                99L,
                1L,
                20L,
                "pack passport",
                false,
                LocalDateTime.of(2026, 7, 1, 9, 0),
                LocalDateTime.of(2026, 6, 23, 10, 0),
                null
        ));

        var response = service.createChecklistItem(1L, 10L, request);

        assertThat(response.checklistItemId()).isEqualTo(99L);
        assertThat(response.title()).isEqualTo("pack passport");
        assertThat(response.assigneeUserId()).isEqualTo(20L);
        assertThat(response.done()).isFalse();

        ArgumentCaptor<ChecklistItem> itemCaptor = ArgumentCaptor.forClass(ChecklistItem.class);
        verify(mapper).insertChecklistItem(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getTripId()).isEqualTo(1L);
        assertThat(itemCaptor.getValue().getTitle()).isEqualTo("pack passport");
        assertThat(itemCaptor.getValue().getAssigneeUserId()).isEqualTo(20L);
    }

    @Test
    void rejectsBlankTitle() {
        ChecklistMapper mapper = mock(ChecklistMapper.class);
        ChecklistService service = new ChecklistService(mapper);

        assertThatThrownBy(() -> service.createChecklistItem(
                1L,
                10L,
                new ChecklistItemCreateRequest("  ", null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void findsChecklistItemsForAccessibleTrip() {
        ChecklistMapper mapper = mock(ChecklistMapper.class);
        ChecklistService service = new ChecklistService(mapper);

        when(mapper.existsAccessibleTrip(1L, 10L)).thenReturn(true);
        when(mapper.findChecklistItemsByTripId(1L)).thenReturn(List.of(new ChecklistItem(
                99L,
                1L,
                null,
                "book tickets",
                false,
                null,
                LocalDateTime.of(2026, 6, 23, 10, 0),
                null
        )));

        var response = service.findChecklistItems(1L, 10L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).title()).isEqualTo("book tickets");
    }

    @Test
    void deletesChecklistItemFromEditableTrip() {
        ChecklistMapper mapper = mock(ChecklistMapper.class);
        ChecklistService service = new ChecklistService(mapper);

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(true);
        when(mapper.deleteChecklistItem(1L, 99L)).thenReturn(1);

        service.deleteChecklistItem(1L, 99L, 10L);

        verify(mapper).deleteChecklistItem(1L, 99L);
    }

    @Test
    void rejectsDeleteWhenTripIsNotEditable() {
        ChecklistMapper mapper = mock(ChecklistMapper.class);
        ChecklistService service = new ChecklistService(mapper);

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteChecklistItem(1L, 99L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
