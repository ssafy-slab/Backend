package com.ssafy.ssafy_slap.place.dto;

import java.util.List;

public record PlaceFilterResponse(
        List<PlaceCategoryResponse> categories,
        List<RegionFilterResponse> regions
) {
}
