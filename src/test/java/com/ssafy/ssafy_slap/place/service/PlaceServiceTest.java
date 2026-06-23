package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PlaceServiceTest {

    @Test
    void normalizesLeisureSportsLabelToDatabaseCategory() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        PlaceSearchRequest request = new PlaceSearchRequest("레저스포츠", null, null, null, 0, 20);

        assertThat(placeService.normalizeCategory(request.category())).isEqualTo("레포츠");
    }

    @Test
    void buildsOffsetFromPageAndSize() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        PlaceSearchRequest request = new PlaceSearchRequest(null, null, null, null, 2, 30);

        assertThat(placeService.toOffset(request)).isEqualTo(60);
    }

    @Test
    void keepsKeywordAsSingleSearchTermWithoutTokenExpansion() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        var tokens = placeService.toSearchTokens("강릉 카페");

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).terms()).containsExactly("강릉 카페");
        assertThat(tokens.get(0).category()).isNull();
        assertThat(tokens.get(0).cat3()).isNull();
        assertThat(tokens.get(0).lclsSystm2()).isNull();
    }

    @Test
    void acceptsOnlySupportedReviewSorts() {
        assertThat(new PlaceSearchRequest(null, null, null, "recommended", 0, 20).normalizedSort()).isEqualTo("recommended");
        assertThat(new PlaceSearchRequest(null, null, null, "reviewCount", 0, 20).normalizedSort()).isEqualTo("reviewCount");
        assertThat(new PlaceSearchRequest(null, null, null, "rating", 0, 20).normalizedSort()).isEqualTo("rating");
        assertThat(new PlaceSearchRequest(null, null, null, "unknown", 0, 20).normalizedSort()).isNull();
    }
}
