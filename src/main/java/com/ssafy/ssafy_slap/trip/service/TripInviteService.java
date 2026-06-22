package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripInviteCode;
import com.ssafy.ssafy_slap.trip.dto.TripInviteCodeResponse;
import com.ssafy.ssafy_slap.trip.dto.TripJoinRequest;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.mapper.TripInviteMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class TripInviteService {

    private static final String TEAM_TRIP_TYPE = "TEAM";
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final TripInviteMapper tripInviteMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public TripInviteService(TripInviteMapper tripInviteMapper) {
        this.tripInviteMapper = tripInviteMapper;
    }

    @Transactional
    public TripInviteCodeResponse createInviteCode(Long tripId, Long userId) {
        validateTripId(tripId);
        validateUserId(userId);

        Trip trip = tripInviteMapper.findTripById(tripId);
        if (trip == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
        }
        validateTeamTrip(trip);
        if (!userId.equals(trip.getOwnerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trip owner can create invite code");
        }

        TripInviteCode existingCode = tripInviteMapper.findActiveInviteCodeByTripId(tripId);
        if (existingCode != null) {
            return new TripInviteCodeResponse(tripId, existingCode.getInviteCode());
        }

        String inviteCode = generateUniqueInviteCode();
        tripInviteMapper.insertInviteCode(new TripInviteCode(
                null,
                tripId,
                inviteCode,
                userId,
                "ACTIVE",
                null
        ));
        return new TripInviteCodeResponse(tripId, inviteCode);
    }

    @Transactional
    public TripResponse joinTrip(Long userId, TripJoinRequest request) {
        validateUserId(userId);
        String inviteCode = normalizeInviteCode(request);

        Trip trip = tripInviteMapper.findTripByInviteCode(inviteCode);
        if (trip == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code not found");
        }
        validateTeamTrip(trip);

        if (!tripInviteMapper.existsTripMember(trip.getTripId(), userId)) {
            tripInviteMapper.insertTripMember(trip.getTripId(), userId, "EDITOR", "ACCEPTED");
        }
        return TripResponse.from(trip);
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String inviteCode = generateInviteCode();
            if (!tripInviteMapper.existsInviteCode(inviteCode)) {
                return inviteCode;
            }
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not generate invite code");
    }

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private String normalizeInviteCode(TripJoinRequest request) {
        if (request == null || request.inviteCode() == null || request.inviteCode().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inviteCode is required");
        }
        return request.inviteCode().trim().toUpperCase(Locale.ROOT);
    }

    private void validateTeamTrip(Trip trip) {
        if (!TEAM_TRIP_TYPE.equalsIgnoreCase(trip.getTripType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite code is only available for team trip");
        }
    }

    private void validateTripId(Long tripId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }
}
