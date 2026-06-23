package com.ssafy.ssafy_slap.trip.mapper;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripScheduleMapper {

    boolean existsAccessibleTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsEditableTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsPlace(@Param("placeId") Long placeId);

    void insertScheduleItem(@Param("item") TripScheduleItem item);

    TripScheduleItem findScheduleItemById(@Param("scheduleItemId") Long scheduleItemId);

    List<TripScheduleItem> findScheduleItemsByTripId(@Param("tripId") Long tripId);

    int updateScheduleItem(@Param("item") TripScheduleItem item);

    int deleteScheduleItem(
            @Param("tripId") Long tripId,
            @Param("scheduleItemId") Long scheduleItemId
    );
}
