package com.ssafy.ssafy_slap.trip.dto;

import com.ssafy.ssafy_slap.trip.domain.TripMember;

import java.time.LocalDateTime;

public record TripMemberResponse(
        Long userId,
        String nickname,
        String memberRole,
        String inviteStatus,
        LocalDateTime joinedAt
) {

    public static TripMemberResponse from(TripMember member) {
        return new TripMemberResponse(
                member.getUserId(),
                member.getNickname(),
                member.getMemberRole(),
                member.getInviteStatus(),
                member.getJoinedAt()
        );
    }
}
