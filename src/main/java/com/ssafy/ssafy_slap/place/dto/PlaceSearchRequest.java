package com.ssafy.ssafy_slap.place.dto;

public record PlaceSearchRequest(
        String category,
        Long regionId,
        String keyword,
        String sort,
        String searchMode,
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

    public String normalizedSort() {
        if ("recommended".equals(sort) || "reviewCount".equals(sort) || "rating".equals(sort) || "random".equals(sort)) {
            return sort;
        }
        return null;
    }

    public boolean tokenizedSearch() {
        return "tokenized".equals(searchMode);
    }
}
