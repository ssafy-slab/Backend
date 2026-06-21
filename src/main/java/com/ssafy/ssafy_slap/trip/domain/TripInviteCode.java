package com.ssafy.ssafy_slap.trip.domain;

import java.time.LocalDateTime;

public class TripInviteCode {

    private Long tripInviteCodeId;
    private Long tripId;
    private String inviteCode;
    private Long createdByUserId;
    private String status;
    private LocalDateTime createdAt;

    public TripInviteCode() {
    }

    public TripInviteCode(
            Long tripInviteCodeId,
            Long tripId,
            String inviteCode,
            Long createdByUserId,
            String status,
            LocalDateTime createdAt
    ) {
        this.tripInviteCodeId = tripInviteCodeId;
        this.tripId = tripId;
        this.inviteCode = inviteCode;
        this.createdByUserId = createdByUserId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getTripInviteCodeId() {
        return tripInviteCodeId;
    }

    public void setTripInviteCodeId(Long tripInviteCodeId) {
        this.tripInviteCodeId = tripInviteCodeId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
