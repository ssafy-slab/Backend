package com.ssafy.ssafy_slap.place.dto;

import java.util.List;

public record PlacePageResponse(
        List<PlaceSummaryResponse> content,
        long totalElements,
        int page,
        int size,
        boolean hasNext
) {
}
