package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceFilterResponse;
import com.ssafy.ssafy_slap.place.dto.PlacePageResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchToken;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        var content = placeMapper.findPlaces(category, request.regionId(), searchTokens, size, offset);
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

        List<PlaceSearchToken> tokens = new ArrayList<>();
        for (String rawToken : normalized.split("\\s+")) {
            String token = normalizeText(rawToken);
            if (token == null) {
                continue;
            }
            tokens.add(expandToken(token));
        }
        return tokens;
    }

    private PlaceSearchToken expandToken(String token) {
        return switch (token) {
            case "카페", "커피", "디저트", "베이커리", "빵집" ->
                    new PlaceSearchToken(List.of("카페", "커피"), null, "A05020900", "FD05");
            case "맛집", "식당", "밥집", "음식", "음식점" ->
                    new PlaceSearchToken(List.of(token), "음식점", null, null);
            case "숙소", "숙박", "호텔", "펜션", "리조트" ->
                    new PlaceSearchToken(List.of(token), "숙박", null, null);
            case "레저", "레저스포츠", "액티비티", "스포츠" ->
                    new PlaceSearchToken(List.of(token), "레포츠", null, null);
            case "관광", "관광지", "명소", "여행지" ->
                    new PlaceSearchToken(List.of(token), "관광지", null, null);
            case "쇼핑", "시장", "몰" ->
                    new PlaceSearchToken(List.of(token), "쇼핑", null, null);
            case "문화", "문화시설", "박물관", "미술관" ->
                    new PlaceSearchToken(List.of(token), "문화시설", null, null);
            default -> new PlaceSearchToken(List.of(token), null, null, null);
        };
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
