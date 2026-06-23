package com.ssafy.ssafy_slap.community.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class CommunityImageStorageServiceTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final CommunityImageStorageService service = new CommunityImageStorageService(
            s3Client,
            "ssafyslapbucket",
            "ap-northeast-2",
            ""
    );

    @Test
    void uploadsImageToS3AndReturnsPublicUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "beach.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        var response = service.store(file);

        assertThat(response.imageUrl())
                .startsWith("https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/")
                .endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsImagesLargerThanTwoMb() {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "large.jpg",
                "image/jpeg",
                new byte[(2 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void deletesOwnedS3ImageByUrl() {
        service.deleteIfOwnedS3Image("https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/a.png");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void ignoresNonCommunityOrExternalImageUrls() {
        service.deleteIfOwnedS3Image("https://example.com/community/a.png");
        service.deleteIfOwnedS3Image("https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/other/a.png");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void logsAndContinuesWhenS3DeleteFails() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("denied").build());

        service.deleteIfOwnedS3Image("https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/a.png");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
