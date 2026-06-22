package com.ssafy.ssafy_slap.community.service;

import com.ssafy.ssafy_slap.community.dto.CommunityImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class CommunityImageStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final Path uploadRoot;

    public CommunityImageStorageService(
            @Value("${community.upload-dir:uploads/community}") String uploadDir
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public CommunityImageResponse store(MultipartFile file) {
        validate(file);
        String extension = EXTENSIONS.get(file.getContentType());
        String filename = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadRoot);
            Path target = uploadRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            file.transferTo(target);
            return new CommunityImageResponse("/uploads/community/" + filename);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image", ex);
        }
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
}
