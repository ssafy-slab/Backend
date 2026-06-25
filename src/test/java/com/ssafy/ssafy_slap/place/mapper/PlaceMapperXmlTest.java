package com.ssafy.ssafy_slap.place.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceMapperXmlTest {

    @Test
    void containsSupportedReviewSortBranches() throws Exception {
        String xml = Files.readString(Path.of("src/main/resources/mapper/place/PlaceMapper.xml"));

        assertThat(xml).contains("sort == 'recommended'");
        assertThat(xml).contains("sort == 'reviewCount'");
        assertThat(xml).contains("sort == 'rating'");
        assertThat(xml).contains("sort == 'random'");
        assertThat(xml).contains("RAND()");
        assertThat(xml).contains("recommendation_score");
    }
}
