package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiPlaceCandidate;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class AiPlaceMatcher {

    private final PlaceMapper placeMapper;

    public AiPlaceMatcher(PlaceMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    public Long findPlaceId(String placeName, String regionHint) {
        if (placeName == null || placeName.isBlank()) {
            return null;
        }
        List<AiPlaceCandidate> candidates = placeMapper.findAiPlaceCandidates(placeName.trim());
        if (candidates.size() == 1) {
            return regionMatches(candidates.get(0), regionHint) ? candidates.get(0).placeId() : null;
        }
        if (regionHint == null || regionHint.isBlank()) {
            return null;
        }
        List<AiPlaceCandidate> matches = candidates.stream()
                .filter(candidate -> regionMatches(candidate, regionHint))
                .toList();
        return matches.size() == 1 ? matches.get(0).placeId() : null;
    }

    private boolean regionMatches(AiPlaceCandidate candidate, String regionHint) {
        if (regionHint == null || regionHint.isBlank()) {
            return true;
        }
        String searchable = normalize(
                nullToEmpty(candidate.regionName()) + " "
                        + nullToEmpty(candidate.regionFullName()) + " "
                        + nullToEmpty(candidate.address())
        );
        return Arrays.stream(regionHint.trim().split("[\\s,]+"))
                .map(this::normalize)
                .filter(token -> !token.isBlank())
                .allMatch(searchable::contains);
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
