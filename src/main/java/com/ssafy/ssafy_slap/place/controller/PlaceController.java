package com.ssafy.ssafy_slap.place.controller;

import com.ssafy.ssafy_slap.place.dto.PlaceFilterResponse;
import com.ssafy.ssafy_slap.place.dto.PlacePageResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.dto.PlaceSummaryResponse;
import com.ssafy.ssafy_slap.place.service.PlaceService;
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

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    public PlacePageResponse searchPlaces(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        return placeService.searchPlaces(new PlaceSearchRequest(category, regionId, keyword, page, size));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceSummaryResponse> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(placeService.getPlace(placeId));
    }

    @GetMapping("/filters")
    public PlaceFilterResponse getFilters() {
        return placeService.getFilters();
    }
}
