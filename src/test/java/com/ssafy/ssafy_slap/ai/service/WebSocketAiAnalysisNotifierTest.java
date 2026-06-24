package com.ssafy.ssafy_slap.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.chat.service.ChatRoomSessionRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketAiAnalysisNotifierTest {

    @Test
    void broadcastsNoResultPayload() throws Exception {
        ChatRoomSessionRegistry registry = mock(ChatRoomSessionRegistry.class);
        WebSocketAiAnalysisNotifier notifier = new WebSocketAiAnalysisNotifier(registry);

        notifier.noResult(
                1L,
                12L,
                "NO_SCHEDULE_CONTEXT",
                "메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다."
        );

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(registry).broadcast(org.mockito.ArgumentMatchers.eq(1L), messageCaptor.capture());
        JsonNode payload = new ObjectMapper().readTree(messageCaptor.getValue().getPayload());
        assertThat(payload.path("type").asText()).isEqualTo("AI_ANALYSIS_NO_RESULT");
        assertThat(payload.path("tripId").asLong()).isEqualTo(1L);
        assertThat(payload.path("analysisRunId").asLong()).isEqualTo(12L);
        assertThat(payload.path("reasonCode").asText()).isEqualTo("NO_SCHEDULE_CONTEXT");
        assertThat(payload.path("message").asText())
                .isEqualTo("메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다.");
    }
}
