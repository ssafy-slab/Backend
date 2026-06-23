package com.ssafy.ssafy_slap.ai.dto;

public record AiPlaceCandidate(
        Long placeId,
        String placeName,
        String regionName,
        String regionFullName,
        String address
) {
}
