package com.ssafy.ssafy_slap.place.dto;

public record PlaceSearchRequest(
        String category,
        Long regionId,
        String keyword,
        Integer page,
        Integer size
) {
    public int normalizedPage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int normalizedSize() {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 50);
    }
}
