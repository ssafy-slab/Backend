package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.NearbyFacilityResponse;
import com.ssafy.ssafy_slap.place.dto.PlaceWeatherPoint;
import com.ssafy.ssafy_slap.place.mapper.PlaceMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceNearbyFacilityServiceTest {

    @Test
    void returnsCachedFacilitiesWithoutCallingKakao() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        KakaoLocalClient kakaoLocalClient = mock(KakaoLocalClient.class);
        PlaceNearbyFacilityService service = new PlaceNearbyFacilityService(placeMapper, kakaoLocalClient, 7);

        when(placeMapper.findWeatherPointById(1L)).thenReturn(new PlaceWeatherPoint(
                1L,
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));
        when(placeMapper.countValidNearbyFacilityCache(eq(1L), eq(NearbyFacilityType.PHARMACY), eq(1000), any()))
                .thenReturn(1);
        when(placeMapper.findNearbyFacilities(eq(1L), eq(NearbyFacilityType.PHARMACY), eq(1000), eq(10)))
                .thenReturn(List.of(new NearbyFacilityResponse(
                        10L,
                        "PHARMACY",
                        "PM9",
                        "의료,건강 > 약국",
                        "테스트약국",
                        "02-000-0000",
                        "서울 중구 테스트로 1",
                        "서울 중구 테스트로 1",
                        BigDecimal.valueOf(37.5660),
                        BigDecimal.valueOf(126.9785),
                        "https://place.map.kakao.com/1",
                        120
                )));

        var response = service.getNearbyFacilities(1L, 1000, 10, "PHARMACY");

        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).cached()).isTrue();
        assertThat(response.groups().get(0).facilities()).hasSize(1);
        verify(kakaoLocalClient, never()).searchByCategory(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void refreshesFacilitiesWhenCacheIsMissing() {
        PlaceMapper placeMapper = mock(PlaceMapper.class);
        KakaoLocalClient kakaoLocalClient = mock(KakaoLocalClient.class);
        PlaceNearbyFacilityService service = new PlaceNearbyFacilityService(placeMapper, kakaoLocalClient, 7);

        when(placeMapper.findWeatherPointById(1L)).thenReturn(new PlaceWeatherPoint(
                1L,
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));
        when(placeMapper.countValidNearbyFacilityCache(eq(1L), eq(NearbyFacilityType.CONVENIENCE_STORE), eq(500), any()))
                .thenReturn(0);
        when(kakaoLocalClient.searchByCategory(
                eq(NearbyFacilityType.CONVENIENCE_STORE),
                any(),
                any(),
                eq(500),
                eq(5)
        )).thenReturn(List.of(new KakaoNearbyFacility(
                "100",
                NearbyFacilityType.CONVENIENCE_STORE,
                "가정,생활 > 편의점",
                "테스트편의점",
                "02-111-1111",
                "서울 중구 테스트로 2",
                "서울 중구 테스트로 2",
                BigDecimal.valueOf(37.5661),
                BigDecimal.valueOf(126.9786),
                "https://place.map.kakao.com/100",
                80
        )));
        when(placeMapper.findFacilityIdByExternalId("KAKAO", "100")).thenReturn(20L);
        when(placeMapper.findNearbyFacilities(eq(1L), eq(NearbyFacilityType.CONVENIENCE_STORE), eq(500), eq(5)))
                .thenReturn(List.of());

        var response = service.getNearbyFacilities(1L, 500, 5, "CONVENIENCE_STORE");

        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).cached()).isFalse();
        verify(placeMapper).deleteNearbyFacilities(1L, NearbyFacilityType.CONVENIENCE_STORE, 500);
        verify(placeMapper).upsertFacility(any());
        verify(placeMapper).insertNearbyFacility(1L, 20L, NearbyFacilityType.CONVENIENCE_STORE, 80, 500);
        verify(placeMapper).upsertNearbyFacilityCache(1L, NearbyFacilityType.CONVENIENCE_STORE, 500, 1);
    }
}
