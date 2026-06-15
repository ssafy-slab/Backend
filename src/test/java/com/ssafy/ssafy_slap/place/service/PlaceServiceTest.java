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

        PlaceSearchRequest request = new PlaceSearchRequest("레저스포츠", null, null, 0, 20);

        assertThat(placeService.normalizeCategory(request.category())).isEqualTo("레포츠");
    }

    @Test
    void buildsOffsetFromPageAndSize() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        PlaceSearchRequest request = new PlaceSearchRequest(null, null, null, 2, 30);

        assertThat(placeService.toOffset(request)).isEqualTo(60);
    }

    @Test
    void tokenizesKeywordAndExpandsCafeSynonyms() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        PlaceService placeService = new PlaceService(placeMapper);

        var tokens = placeService.toSearchTokens("강릉 카페");

        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0).terms()).containsExactly("강릉");
        assertThat(tokens.get(0).category()).isNull();
        assertThat(tokens.get(0).cat3()).isNull();
        assertThat(tokens.get(0).lclsSystm2()).isNull();
        assertThat(tokens.get(1).terms()).containsExactly("카페", "커피");
        assertThat(tokens.get(1).category()).isNull();
        assertThat(tokens.get(1).cat3()).isEqualTo("A05020900");
        assertThat(tokens.get(1).lclsSystm2()).isEqualTo("FD05");
    }
}
