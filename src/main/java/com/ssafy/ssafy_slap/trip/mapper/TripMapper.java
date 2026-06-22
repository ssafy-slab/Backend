package com.ssafy.ssafy_slap.trip.mapper;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TripMapper {

    void insertTrip(@Param("trip") Trip trip);

    void insertTripMember(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId,
            @Param("memberRole") String memberRole,
            @Param("inviteStatus") String inviteStatus
    );

    Trip findTripById(@Param("tripId") Long tripId);

    List<Trip> findAccessibleTrips(@Param("userId") Long userId);

    List<TripMember> findMembersByTripIds(@Param("tripIds") List<Long> tripIds);

    Trip findAccessibleTripById(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsAccessibleTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    int updateOwnedTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("description") String description,
            @Param("tripType") String tripType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int deleteOwnedTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );
}
