package com.ssafy.ssafy_slap.place.dto;

import java.util.List;

public record PlaceSearchToken(
        List<String> terms,
        String category,
        String cat3,
        String lclsSystm2
) {
}
