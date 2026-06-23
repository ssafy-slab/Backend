package com.ssafy.ssafy_slap.community.mapper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityCommentMapperXmlTest {

    @Test
    void keepsOnlyDeletedCommentsThatHaveChildrenAndHidesTheirContent() throws Exception {
        String mapperXml = Files.readString(
                Path.of("src/main/resources/mapper/community/CommunityMapper.xml")
        );

        assertThat(mapperXml)
                .contains("c.status = 'ACTIVE'")
                .contains("child.parent_comment_id = c.comment_id")
                .contains("ELSE u.nickname")
                .contains("THEN '삭제된 댓글입니다.'")
                .contains("AS deleted");
    }

    @Test
    void returnsReplyTargetAndEditedMetadata() throws Exception {
        String mapperXml = Files.readString(
                Path.of("src/main/resources/mapper/community/CommunityMapper.xml")
        );

        assertThat(mapperXml)
                .contains("AS replyToNickname")
                .contains("AS edited")
                .contains("LEFT JOIN `COMMENT` parent");
    }
}
