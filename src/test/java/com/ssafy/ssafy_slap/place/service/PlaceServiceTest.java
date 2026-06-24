package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceSearchRequest;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceServiceTest {

    @Test
    void normalizesLeisureSportsLabelToDatabaseCategory() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        PlaceSearchRequest request = new PlaceSearchRequest("레저스포츠", null, null, null, null, 0, 20);

        assertThat(placeService.normalizeCategory(request.category())).isEqualTo("레포츠");
    }

    @Test
    void buildsOffsetFromPageAndSize() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        PlaceSearchRequest request = new PlaceSearchRequest(null, null, null, null, null, 2, 30);

        assertThat(placeService.toOffset(request)).isEqualTo(60);
    }

    @Test
    void keepsKeywordAsSingleSearchTermByDefault() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        var tokens = placeService.toSearchTokens("강릉 카페", false);

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).terms()).containsExactly("강릉 카페");
        assertThat(tokens.get(0).category()).isNull();
        assertThat(tokens.get(0).cat3()).isNull();
        assertThat(tokens.get(0).lclsSystm2()).isNull();
    }

    @Test
    void tokenizesAndExpandsExploreKeywordWhenRequested() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        var tokens = placeService.toSearchTokens("강릉 카페", true);

        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).terms()).containsExactly("강릉");
        assertThat(tokens.get(1).terms()).containsExactly("카페", "커피");
        assertThat(tokens.get(1).cat3()).isEqualTo("A05020900");
        assertThat(tokens.get(1).lclsSystm2()).isEqualTo("FD05");
    }

    @Test
    void recognizesOnlyTheTokenizedSearchMode() {
        assertThat(new PlaceSearchRequest(null, null, null, null, "tokenized", 0, 20).tokenizedSearch()).isTrue();
        assertThat(new PlaceSearchRequest(null, null, null, null, null, 0, 20).tokenizedSearch()).isFalse();
        assertThat(new PlaceSearchRequest(null, null, null, null, "unknown", 0, 20).tokenizedSearch()).isFalse();
    }

    @Test
    void acceptsOnlySupportedReviewSorts() {
        assertThat(new PlaceSearchRequest(null, null, null, "recommended", null, 0, 20).normalizedSort()).isEqualTo("recommended");
        assertThat(new PlaceSearchRequest(null, null, null, "reviewCount", null, 0, 20).normalizedSort()).isEqualTo("reviewCount");
        assertThat(new PlaceSearchRequest(null, null, null, "rating", null, 0, 20).normalizedSort()).isEqualTo("rating");
        assertThat(new PlaceSearchRequest(null, null, null, "unknown", null, 0, 20).normalizedSort()).isNull();
    }

    @Test
    void likesAndRemovesLikeForExistingPlace() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);
        when(placeMapper.existsPlace(3L)).thenReturn(true);

        placeService.likePlace(3L, 7L);
        placeService.removeLike(3L, 7L);

        verify(placeMapper).insertLike(3L, 7L);
        verify(placeMapper).deleteLike(3L, 7L);
    }

    @Test
    void listsLikedPlacesWithSafePaging() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);
        when(placeMapper.findLikedPlaces(7L, 50, 0)).thenReturn(java.util.List.of());

        assertThat(placeService.findLikedPlaces(7L, -1, 100)).isEmpty();
        verify(placeMapper).findLikedPlaces(7L, 50, 0);
    }
}
