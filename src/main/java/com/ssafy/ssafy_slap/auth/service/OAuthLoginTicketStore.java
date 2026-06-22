package com.ssafy.ssafy_slap.auth.service;

import com.ssafy.ssafy_slap.auth.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuthLoginTicketStore {

    private static final Duration TICKET_TTL = Duration.ofMinutes(3);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TicketEntry> tickets = new ConcurrentHashMap<>();

    public String create(AuthResponse response) {
        cleanupExpiredTickets();
        String ticket = createTicket();
        tickets.put(ticket, new TicketEntry(response, Instant.now().plus(TICKET_TTL)));
        return ticket;
    }

    public AuthResponse consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth ticket is required");
        }

        TicketEntry entry = tickets.remove(ticket);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OAuth ticket");
        }
        return entry.response();
    }

    private String createTicket() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void cleanupExpiredTickets() {
        Instant now = Instant.now();
        tickets.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record TicketEntry(AuthResponse response, Instant expiresAt) {
    }
}
