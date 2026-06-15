package com.ssafy.ssafy_slap.place.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class KmaGridConverterTest {

    @Test
    void convertsSeoulLatLonToKmaGrid() {
        KmaGridCoordinate coordinate = KmaGridConverter.fromLatLon(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        );

        assertThat(coordinate.nx()).isEqualTo(60);
        assertThat(coordinate.ny()).isEqualTo(127);
    }
}
