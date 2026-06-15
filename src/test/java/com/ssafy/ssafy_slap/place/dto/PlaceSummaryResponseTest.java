package com.ssafy.ssafy_slap.place.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceSummaryResponseTest {

    @Test
    void exposesPurposeSpecificImageUrls() {
        PlaceSummaryResponse response = new PlaceSummaryResponse(
                1L,
                "place",
                "category",
                10L,
                "region",
                "full region",
                "address",
                null,
                null,
                "description",
                "https://example.com/original.jpg",
                "https://example.com/thumb.jpg",
                "https://example.com/thumb.jpg",
                "https://example.com/thumb.jpg",
                "https://example.com/original.jpg",
                null,
                null
        );

        assertThat(response.thumbnailImageUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(response.detailImageUrl()).isEqualTo("https://example.com/original.jpg");
    }
}
