package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;
import com.ssafy.ssafy_slap.trip.dto.TripMemberRoleUpdateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripMemberResponse;
import com.ssafy.ssafy_slap.trip.mapper.TripMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TripMemberService {

    private static final String OWNER_ROLE = "OWNER";
    private static final String EDITOR_ROLE = "EDITOR";
    private static final String VIEWER_ROLE = "VIEWER";

    private final TripMapper tripMapper;

    public TripMemberService(TripMapper tripMapper) {
        this.tripMapper = tripMapper;
    }

    @Transactional(readOnly = true)
    public List<TripMemberResponse> findMembers(Long tripId, Long userId) {
        validateTripAccess(tripId, userId);
        return tripMapper.findMembersByTripId(tripId)
                .stream()
                .map(TripMemberResponse::from)
                .toList();
    }

    @Transactional
    public void leaveTrip(Long tripId, Long userId) {
        validateTripId(tripId);
        validateUserId(userId);

        TripMember member = tripMapper.findTripMember(tripId, userId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip member not found");
        }
        if (OWNER_ROLE.equalsIgnoreCase(member.getMemberRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip owner cannot leave trip");
        }
        tripMapper.deleteTripMember(tripId, userId);
    }

    @Transactional
    public TripMemberResponse updateMemberRole(
            Long tripId,
            Long memberUserId,
            Long requesterUserId,
            TripMemberRoleUpdateRequest request
    ) {
        validateTripId(tripId);
        validateUserId(memberUserId);
        validateUserId(requesterUserId);
        String memberRole = normalizeEditableRole(request);

        Trip trip = tripMapper.findTripById(tripId);
        if (trip == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
        }
        if (!requesterUserId.equals(trip.getOwnerUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trip owner can update member roles");
        }

        TripMember member = tripMapper.findTripMember(tripId, memberUserId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip member not found");
        }
        if (OWNER_ROLE.equalsIgnoreCase(member.getMemberRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip owner role cannot be changed");
        }

        int updated = tripMapper.updateTripMemberRole(tripId, memberUserId, memberRole);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip member not found");
        }
        member.setMemberRole(memberRole);
        return TripMemberResponse.from(member);
    }

    private void validateTripAccess(Long tripId, Long userId) {
        validateTripId(tripId);
        validateUserId(userId);
        if (!tripMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not accessible");
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

    private String normalizeEditableRole(TripMemberRoleUpdateRequest request) {
        if (request == null || request.memberRole() == null || request.memberRole().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memberRole is required");
        }
        String memberRole = request.memberRole().trim().toUpperCase();
        if (EDITOR_ROLE.equals(memberRole) || VIEWER_ROLE.equals(memberRole)) {
            return memberRole;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memberRole must be EDITOR or VIEWER");
    }
}
