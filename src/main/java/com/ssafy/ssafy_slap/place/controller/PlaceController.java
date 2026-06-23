package com.ssafy.ssafy_slap.place.controller;

import com.ssafy.ssafy_slap.place.dto.PlaceFilterResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceNearbyFacilitiesResponse;
import com.ssafy.ssafy_slap.place.dto.PlacePageResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherResponse;
import com.ssafy.ssafy_slap.place.service.PlaceNearbyFacilityService;
import com.ssafy.ssafy_slap.place.service.PlaceService;
import com.ssafy.ssafy_slap.place.service.PlaceWeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceWeatherService placeWeatherService;
    private final PlaceNearbyFacilityService placeNearbyFacilityService;

    public PlaceController(
            PlaceService placeService,
            PlaceWeatherService placeWeatherService,
            PlaceNearbyFacilityService placeNearbyFacilityService
    ) {
        this.placeService = placeService;
        this.placeWeatherService = placeWeatherService;
        this.placeNearbyFacilityService = placeNearbyFacilityService;
    }

    @GetMapping
    public PlacePageResponse searchPlaces(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String searchMode,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return placeService.searchPlaces(new PlaceSearchRequest(category, regionId, keyword, sort, searchMode, page, size));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceSummaryResponse> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(placeService.getPlace(placeId));
    }

    @GetMapping("/{placeId}/weather")
    public PlaceWeatherResponse getPlaceWeather(@PathVariable Long placeId) {
        return placeWeatherService.getWeather(placeId);
    }

    @GetMapping("/{placeId}/nearby-facilities")
    public PlaceNearbyFacilitiesResponse getNearbyFacilities(
            @PathVariable Long placeId,
            @RequestParam(required = false) Integer radiusM,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String types
    ) {
        return placeNearbyFacilityService.getNearbyFacilities(placeId, radiusM, limit, types);
    }

    @GetMapping("/filters")
    public PlaceFilterResponse getFilters() {
        return placeService.getFilters();
    }
}
