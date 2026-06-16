package com.ssafy.ssafy_slap.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatWebSocketRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatWebSocketResponse;
import com.ssafy.ssafy_slap.chat.service.ChatRoomSessionRegistry;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatRoomSessionRegistry sessionRegistry;

    public ChatWebSocketHandler(
            ChatService chatService,
            ChatRoomSessionRegistry sessionRegistry
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatWebSocketRequest request = objectMapper.readValue(message.getPayload(), ChatWebSocketRequest.class);
            if (request.isSubscribe()) {
                validateTripId(request.tripId());
                sessionRegistry.subscribe(request.tripId(), session);
                session.sendMessage(toTextMessage(ChatWebSocketResponse.subscribed(request.tripId())));
                return;
            }
            if (!request.isChat()) {
                throw new IllegalArgumentException("unsupported websocket message type: " + request.type());
            }

            var response = chatService.createTextMessage(new ChatMessageRequest(
                    request.tripId(),
                    request.senderUserId(),
                    request.content()
            ));
            sessionRegistry.subscribe(response.tripId(), session);
            sessionRegistry.broadcast(response.tripId(), toTextMessage(ChatWebSocketResponse.message(response)));
        } catch (IllegalArgumentException exception) {
            session.sendMessage(toTextMessage(ChatWebSocketResponse.error(exception.getMessage())));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unsubscribe(session);
    }

    private TextMessage toTextMessage(ChatWebSocketResponse response) throws Exception {
        return new TextMessage(objectMapper.writeValueAsString(response));
    }

    private void validateTripId(Long tripId) {
        if (tripId == null) {
            throw new IllegalArgumentException("tripId is required");
        }
    }
}
