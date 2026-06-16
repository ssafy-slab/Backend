package com.ssafy.ssafy_slap.chat.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomSessionRegistry {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByTrip = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tripBySessionId = new ConcurrentHashMap<>();

    public void subscribe(Long tripId, WebSocketSession session) {
        unsubscribe(session);
        sessionsByTrip.computeIfAbsent(tripId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        tripBySessionId.put(session.getId(), tripId);
    }

    public void unsubscribe(WebSocketSession session) {
        Long tripId = tripBySessionId.remove(session.getId());
        if (tripId == null) {
            return;
        }

        Set<WebSocketSession> sessions = sessionsByTrip.get(tripId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByTrip.remove(tripId, sessions);
        }
    }

    public void broadcast(Long tripId, TextMessage message) throws IOException {
        Set<WebSocketSession> sessions = sessionsByTrip.get(tripId);
        if (sessions == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }
}
