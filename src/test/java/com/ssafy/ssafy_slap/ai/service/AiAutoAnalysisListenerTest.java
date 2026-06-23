package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.chat.service.ChatMessageCreatedEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAutoAnalysisListenerTest {
    @Test
    void startsAutomaticAnalysisWhenThirtyMessagesArePending() {
        AiAnalysisService service = mock(AiAnalysisService.class);
        AiAutoAnalysisListener listener = new AiAutoAnalysisListener(service);
        when(service.shouldAutoAnalyze(1L)).thenReturn(true);

        listener.onMessageCreated(new ChatMessageCreatedEvent(1L, 7L, 30L));

        verify(service).analyzeAuto(1L, 7L);
    }
}
