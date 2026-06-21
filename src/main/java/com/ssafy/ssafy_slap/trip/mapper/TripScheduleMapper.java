package com.ssafy.ssafy_slap.trip.mapper;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TripScheduleMapper {

    boolean existsAccessibleTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsPlace(@Param("placeId") Long placeId);

    void insertScheduleItem(@Param("item") TripScheduleItem item);

    TripScheduleItem findScheduleItemById(@Param("scheduleItemId") Long scheduleItemId);

    int deleteScheduleItem(
            @Param("tripId") Long tripId,
            @Param("scheduleItemId") Long scheduleItemId
    );
}
