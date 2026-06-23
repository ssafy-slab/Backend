package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiPlaceCandidate;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiPlaceMatcherTest {

    @Test
    void linksUniqueExactPlaceMatchingRegionHint() {
        PlaceMapper mapper = mock(PlaceMapper.class);
        AiPlaceMatcher matcher = new AiPlaceMatcher(mapper);
        when(mapper.findAiPlaceCandidates("해운대해수욕장")).thenReturn(List.of(
                new AiPlaceCandidate(351L, "해운대해수욕장", "해운대구", "부산광역시 해운대구",
                        "부산광역시 해운대구 해운대해변로 264")
        ));

        assertThat(matcher.findPlaceId(" 해운대해수욕장 ", "부산 해운대구")).isEqualTo(351L);
    }

    @Test
    void leavesAmbiguousExactPlaceUnmatched() {
        PlaceMapper mapper = mock(PlaceMapper.class);
        AiPlaceMatcher matcher = new AiPlaceMatcher(mapper);
        when(mapper.findAiPlaceCandidates("중앙공원")).thenReturn(List.of(
                new AiPlaceCandidate(1L, "중앙공원", "중구", "부산광역시 중구", "부산 중구"),
                new AiPlaceCandidate(2L, "중앙공원", "중구", "대전광역시 중구", "대전 중구")
        ));

        assertThat(matcher.findPlaceId("중앙공원", null)).isNull();
    }

    @Test
    void leavesMissingPlaceUnmatched() {
        PlaceMapper mapper = mock(PlaceMapper.class);
        AiPlaceMatcher matcher = new AiPlaceMatcher(mapper);
        when(mapper.findAiPlaceCandidates("없는장소")).thenReturn(List.of());

        assertThat(matcher.findPlaceId("없는장소", "부산")).isNull();
    }
}
