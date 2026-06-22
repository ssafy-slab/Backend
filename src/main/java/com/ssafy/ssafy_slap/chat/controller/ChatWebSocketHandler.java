package com.ssafy.ssafy_slap.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.auth.service.JwtPrincipal;
import com.ssafy.ssafy_slap.auth.service.JwtTokenProvider;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatWebSocketRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatWebSocketResponse;
import com.ssafy.ssafy_slap.chat.service.ChatRoomSessionRegistry;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatRoomSessionRegistry sessionRegistry;
    private final JwtTokenProvider tokenProvider;

    public ChatWebSocketHandler(
            ChatService chatService,
            ChatRoomSessionRegistry sessionRegistry,
            JwtTokenProvider tokenProvider
    ) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.chatService = chatService;
        this.sessionRegistry = sessionRegistry;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Long authenticatedUserId = authenticatedUserId(session);
            ChatWebSocketRequest request = objectMapper.readValue(message.getPayload(), ChatWebSocketRequest.class);
            if (request.isSubscribe()) {
                validateTripId(request.tripId());
                chatService.validateTripAccess(authenticatedUserId, request.tripId());
                sessionRegistry.subscribe(request.tripId(), session);
                log.info(
                        "Chat websocket subscribed tripId={} sessionId={} subscriberCount={}",
                        request.tripId(),
                        session.getId(),
                        sessionRegistry.subscriberCount(request.tripId())
                );
                session.sendMessage(toTextMessage(ChatWebSocketResponse.subscribed(request.tripId())));
                return;
            }
            if (!request.isChat()) {
                throw new IllegalArgumentException("unsupported websocket message type: " + request.type());
            }

            var response = chatService.createTextMessage(authenticatedUserId, new ChatMessageRequest(
                    request.tripId(),
                    authenticatedUserId,
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

    private Long authenticatedUserId(WebSocketSession session) {
        String token = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("token");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Authentication is required");
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            JwtPrincipal principal = tokenProvider.parse(token);
            return principal.userId();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid authentication token");
        }
    }
}
