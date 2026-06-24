package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.dto.CommunityImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommunityImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CommunityImageStorageService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final S3Client s3Client;
    private final String bucket;
    private final String region;
    private final String publicBaseUrl;

    public CommunityImageStorageService(
            S3Client s3Client,
            @Value("${community.s3.bucket}") String bucket,
            @Value("${community.s3.region}") String region,
            @Value("${community.s3.public-base-url:}") String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.strip();
    }

    public CommunityImageResponse store(MultipartFile file) {
        validate(file);
        String contentType = file.getContentType();
        String key = "community/" + UUID.randomUUID() + EXTENSIONS.get(contentType);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return new CommunityImageResponse(publicUrl(key));
        } catch (IOException | S3Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image to S3", ex);
        }
    }

    public void deleteIfOwnedS3Image(String imageUrl) {
        extractOwnedKey(imageUrl).ifPresent(key -> {
            try {
                DeleteObjectRequest request = DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();
                s3Client.deleteObject(request);
            } catch (S3Exception ex) {
                log.warn("Failed to delete S3 community image. bucket={}, key={}", bucket, key, ex);
            }
        });
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file must be 5MB or smaller");
        }
        if (!EXTENSIONS.containsKey(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, WebP, and GIF images are supported");
        }
    }

    private String publicUrl(String key) {
        if (!publicBaseUrl.isBlank()) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + key;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private Optional<String> extractOwnedKey(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return Optional.empty();
        }
        String normalizedPublicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        if (!normalizedPublicBaseUrl.isBlank() && imageUrl.startsWith(normalizedPublicBaseUrl + "/")) {
            return keyFromPath(imageUrl.substring(normalizedPublicBaseUrl.length() + 1));
        }
        try {
            URI uri = new URI(imageUrl);
            String expectedHost = bucket + ".s3." + region + ".amazonaws.com";
            if (!expectedHost.equals(uri.getHost())) {
                return Optional.empty();
            }
            String path = uri.getPath();
            if (path == null || path.length() <= 1) {
                return Optional.empty();
            }
            return keyFromPath(path.substring(1));
        } catch (URISyntaxException ex) {
            return Optional.empty();
        }
    }

    private Optional<String> keyFromPath(String key) {
        if (key == null || key.isBlank() || key.contains("..") || !key.startsWith("community/")) {
            return Optional.empty();
        }
        return Optional.of(key);
    }
}
