package com.ssafy.ssafy_slap.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomSessionRegistry.class);

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByTrip = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tripBySessionId = new ConcurrentHashMap<>();

    public void subscribe(Long tripId, WebSocketSession session) {
        unsubscribe(session);
        sessionsByTrip.computeIfAbsent(tripId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        tripBySessionId.put(session.getId(), tripId);
    }

    public int subscriberCount(Long tripId) {
        Set<WebSocketSession> sessions = sessionsByTrip.get(tripId);
        if (sessions == null) {
            return 0;
        }
        return sessions.size();
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
            log.info("Chat broadcast tripId={} targetSessions=0 sentOpenSessions=0", tripId);
            return;
        }

        int sentOpenSessions = 0;
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                log.warn("Chat broadcast skipped closed session tripId={} sessionId={}", tripId, session.getId());
                continue;
            }

            try {
                session.sendMessage(message);
                sentOpenSessions++;
            } catch (IOException exception) {
                log.warn("Chat broadcast failed tripId={} sessionId={}", tripId, session.getId(), exception);
                log.info(
                        "Chat broadcast tripId={} targetSessions={} sentOpenSessions={}",
                        tripId,
                        sessions.size(),
                        sentOpenSessions
                );
                throw exception;
            }
        }
        log.info(
                "Chat broadcast tripId={} targetSessions={} sentOpenSessions={}",
                tripId,
                sessions.size(),
                sentOpenSessions
        );
    }
}
