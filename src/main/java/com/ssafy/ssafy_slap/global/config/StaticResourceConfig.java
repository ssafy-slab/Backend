package com.ssafy.ssafy_slap.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String communityUploadDir;

    public StaticResourceConfig(
            @Value("${community.upload-dir:uploads/community}") String communityUploadDir
    ) {
        this.communityUploadDir = communityUploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(communityUploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/community/**")
                .addResourceLocations(location);
    }
}
