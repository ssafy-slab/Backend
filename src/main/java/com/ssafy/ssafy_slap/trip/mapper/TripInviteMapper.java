package com.ssafy.ssafy_slap.trip.mapper;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripInviteCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TripInviteMapper {

    Trip findTripById(@Param("tripId") Long tripId);

    TripInviteCode findActiveInviteCodeByTripId(@Param("tripId") Long tripId);

    boolean existsInviteCode(@Param("inviteCode") String inviteCode);

    void insertInviteCode(@Param("inviteCode") TripInviteCode inviteCode);

    Trip findTripByInviteCode(@Param("inviteCode") String inviteCode);

    boolean existsTripMember(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    void insertTripMember(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId,
            @Param("memberRole") String memberRole,
            @Param("inviteStatus") String inviteStatus
    );
}
