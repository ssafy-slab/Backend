package com.ssafy.ssafy_slap.trip.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TripScheduleMapperXmlTest {

    @Test
    void treatsLegacyMemberRoleAsEditable() throws Exception {
        String mapperXml = Files.readString(
                Path.of("src/main/resources/mapper/trip/TripScheduleMapper.xml")
        );

        assertThat(mapperXml)
                .contains("tm.member_role IN ('EDITOR', 'MEMBER')");
    }
}
