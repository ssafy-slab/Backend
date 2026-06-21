package com.ssafy.ssafy_slap.trip.domain;

import java.time.LocalDateTime;

public class TripMember {

    private Long tripMemberId;
    private Long tripId;
    private Long userId;
    private String nickname;
    private String memberRole;
    private String inviteStatus;
    private LocalDateTime joinedAt;

    public TripMember() {
    }

    public TripMember(
            Long tripMemberId,
            Long tripId,
            Long userId,
            String nickname,
            String memberRole,
            String inviteStatus,
            LocalDateTime joinedAt
    ) {
        this.tripMemberId = tripMemberId;
        this.tripId = tripId;
        this.userId = userId;
        this.nickname = nickname;
        this.memberRole = memberRole;
        this.inviteStatus = inviteStatus;
        this.joinedAt = joinedAt;
    }

    public Long getTripMemberId() {
        return tripMemberId;
    }

    public void setTripMemberId(Long tripMemberId) {
        this.tripMemberId = tripMemberId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public String getInviteStatus() {
        return inviteStatus;
    }

    public void setInviteStatus(String inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
