package com.ssafy.ssafy_slap.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.chat.service.ChatRoomSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.util.Map;

@Component
public class WebSocketAiAnalysisNotifier implements AiAnalysisNotifier {
    private static final Logger log = LoggerFactory.getLogger(WebSocketAiAnalysisNotifier.class);
    private final ChatRoomSessionRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketAiAnalysisNotifier(ChatRoomSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void completed(Long tripId, Long analysisRunId) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "AI_ANALYSIS_COMPLETED",
                    "tripId", tripId,
                    "analysisRunId", analysisRunId
            ));
            registry.broadcast(tripId, new TextMessage(json));
        } catch (Exception exception) {
            log.warn("Failed to broadcast AI analysis completion tripId={} runId={}",
                    tripId, analysisRunId, exception);
        }
    }
}
