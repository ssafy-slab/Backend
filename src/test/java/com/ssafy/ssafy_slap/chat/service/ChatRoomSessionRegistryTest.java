package com.ssafy.ssafy_slap.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatRoomSessionRegistryTest {

    @Test
    void broadcastsOnlyToSessionsInTripRoom() throws Exception {
        ChatRoomSessionRegistry registry = new ChatRoomSessionRegistry();
        WebSocketSession tripOneSession = mock(WebSocketSession.class);
        WebSocketSession tripTwoSession = mock(WebSocketSession.class);

        when(tripOneSession.getId()).thenReturn("session-1");
        when(tripOneSession.isOpen()).thenReturn(true);
        when(tripTwoSession.getId()).thenReturn("session-2");
        when(tripTwoSession.isOpen()).thenReturn(true);

        registry.subscribe(1L, tripOneSession);
        registry.subscribe(2L, tripTwoSession);

        TextMessage message = new TextMessage("{\"content\":\"hello\"}");
        registry.broadcast(1L, message);

        verify(tripOneSession).sendMessage(message);
        verify(tripTwoSession, never()).sendMessage(message);
    }
}
