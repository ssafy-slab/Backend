package com.ssafy.ssafy_slap.chat.controller;

import com.ssafy.ssafy_slap.auth.service.JwtPrincipal;
import com.ssafy.ssafy_slap.auth.service.JwtTokenProvider;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.service.ChatRoomSessionRegistry;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatWebSocketHandlerTest {

    @Test
    void createsMessageWithAuthenticatedUserIdFromToken() throws Exception {
        ChatService chatService = mock(ChatService.class);
        ChatRoomSessionRegistry sessionRegistry = mock(ChatRoomSessionRegistry.class);
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, sessionRegistry, tokenProvider);
        WebSocketSession session = mock(WebSocketSession.class);

        when(session.getUri()).thenReturn(URI.create("ws://localhost:8080/ws/chats?token=token-123"));
        when(tokenProvider.parse("token-123")).thenReturn(new JwtPrincipal(8L, "USER"));
        when(chatService.createTextMessage(any(), any())).thenReturn(new ChatMessageResponse(
                1L,
                7L,
                8L,
                "tester",
                "TEXT",
                "hello",
                LocalDateTime.of(2026, 6, 22, 13, 0)
        ));

        handler.handleTextMessage(session, new TextMessage("""
                {"type":"CHAT","tripId":7,"senderUserId":999,"content":"hello"}
                """));

        ArgumentCaptor<ChatMessageRequest> requestCaptor = ArgumentCaptor.forClass(ChatMessageRequest.class);
        verify(chatService).createTextMessage(org.mockito.Mockito.eq(8L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().senderUserId()).isEqualTo(8L);
    }
}
