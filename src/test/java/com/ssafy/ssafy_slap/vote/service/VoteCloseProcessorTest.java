package com.ssafy.ssafy_slap.vote.service;

import com.ssafy.ssafy_slap.vote.domain.Vote;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VoteCloseProcessorTest {

    @Test
    void processesLinkedOutcomeBeforeClosingVote() {
        VoteMapper mapper = mock(VoteMapper.class);
        VoteCloseProcessor processor = mock(VoteCloseProcessor.class);
        VoteService service = new VoteService(mapper, List.of(processor));

        when(mapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(mapper.findVote(1L, 20L))
                .thenReturn(new Vote(20L, 1L, 7L, "AI 제안", "OPEN", null, null))
                .thenReturn(new Vote(20L, 1L, 7L, "AI 제안", "CLOSED", null, null));
        when(mapper.closeVote(1L, 20L)).thenReturn(1);
        when(mapper.findOptionsWithCounts(20L)).thenReturn(List.of());

        service.closeVote(1L, 20L, 7L);

        var order = inOrder(processor, mapper);
        order.verify(processor).beforeClose(1L, 20L, 7L);
        order.verify(mapper).closeVote(1L, 20L);
    }
}
