package com.ssafy.ssafy_slap.user.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperXmlTest {

    @Test
    void oauthAccountLookupSelectsColumnsFromAppUserAlias() throws Exception {
        String mapperXml = Files.readString(Path.of("src/main/resources/mapper/user/UserMapper.xml"));

        assertThat(mapperXml).contains("u.user_id AS userId");
        assertThat(mapperXml).contains("u.email AS email");
        assertThat(mapperXml).contains("u.password_hash AS passwordHash");
        assertThat(mapperXml).contains("INNER JOIN OAUTH_ACCOUNT oa");
    }
}
