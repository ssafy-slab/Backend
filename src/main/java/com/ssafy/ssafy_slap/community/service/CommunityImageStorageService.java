package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.dto.CommunityImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CommunityImageStorageService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
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

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image file must be 2MB or smaller");
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
}
