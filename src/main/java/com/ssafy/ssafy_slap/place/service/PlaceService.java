package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceFilterResponse;
import com.ssafy.ssafy_slap.place.dto.PlacePageResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchToken;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceMapper placeMapper;

    public PlaceService(PlaceMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    public PlacePageResponse searchPlaces(PlaceSearchRequest request) {
        int page = request.normalizedPage();
        int size = request.normalizedSize();
        int offset = toOffset(request);
        String category = normalizeCategory(request.category());
        List<PlaceSearchToken> searchTokens = toSearchTokens(request.keyword());

        var content = placeMapper.findPlaces(category, request.regionId(), searchTokens, request.normalizedSort(), size, offset);
        long totalElements = placeMapper.countPlaces(category, request.regionId(), searchTokens);
        boolean hasNext = (long) offset + content.size() < totalElements;

        return new PlacePageResponse(content, totalElements, page, size, hasNext);
    }

    public PlaceSummaryResponse getPlace(Long placeId) {
        PlaceSummaryResponse place = placeMapper.findById(placeId);
        if (place == null) {
            throw new NoSuchElementException("Place not found: " + placeId);
        }
        return place;
    }

    public PlaceFilterResponse getFilters() {
        return new PlaceFilterResponse(placeMapper.findCategories(), placeMapper.findRegions());
    }

    public String normalizeCategory(String category) {
        String normalized = normalizeText(category);
        if (normalized == null) {
            return null;
        }
        if ("레저스포츠".equals(normalized)) {
            return "레포츠";
        }
        return normalized;
    }

    public int toOffset(PlaceSearchRequest request) {
        return request.normalizedPage() * request.normalizedSize();
    }

    public List<PlaceSearchToken> toSearchTokens(String keyword) {
        String normalized = normalizeText(keyword);
        if (normalized == null) {
            return List.of();
        }

        return List.of(new PlaceSearchToken(List.of(normalized), null, null, null));
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
