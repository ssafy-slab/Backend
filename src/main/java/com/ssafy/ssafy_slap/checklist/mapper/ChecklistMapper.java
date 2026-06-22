package com.ssafy.ssafy_slap.checklist.mapper;

import com.ssafy.ssafy_slap.checklist.domain.ChecklistItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChecklistMapper {

    boolean existsAccessibleTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsEditableTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    boolean existsTripMember(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    void insertChecklistItem(@Param("item") ChecklistItem item);

    ChecklistItem findChecklistItemById(@Param("checklistItemId") Long checklistItemId);

    List<ChecklistItem> findChecklistItemsByTripId(@Param("tripId") Long tripId);

    int deleteChecklistItem(
            @Param("tripId") Long tripId,
            @Param("checklistItemId") Long checklistItemId
    );
}
