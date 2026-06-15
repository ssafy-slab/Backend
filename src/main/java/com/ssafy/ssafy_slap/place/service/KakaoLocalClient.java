package com.ssafy.ssafy_slap.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class KakaoLocalClient {

    private static final int MAX_SIZE = 15;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String restApiKey;
    private final String categorySearchUrl;

    @Autowired
    public KakaoLocalClient(
            @Value("${kakao.local.rest-api-key:}") String restApiKey,
            @Value("${kakao.local.category-search-url:https://dapi.kakao.com/v2/local/search/category.json}") String categorySearchUrl
    ) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), new ObjectMapper(), restApiKey, categorySearchUrl);
    }

    KakaoLocalClient(HttpClient httpClient, ObjectMapper objectMapper, String restApiKey, String categorySearchUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.restApiKey = restApiKey;
        this.categorySearchUrl = categorySearchUrl;
    }

    public List<KakaoNearbyFacility> searchByCategory(
            NearbyFacilityType facilityType,
            BigDecimal latitude,
            BigDecimal longitude,
            int radiusM,
            int limit
    ) {
        if (!StringUtils.hasText(restApiKey)) {
            throw new IllegalStateException("Kakao REST API key is not configured.");
        }
        int size = Math.max(1, Math.min(limit, MAX_SIZE));
        URI uri = UriComponentsBuilder.fromUriString(categorySearchUrl)
                .queryParam("category_group_code", facilityType.categoryGroupCode())
                .queryParam("x", longitude)
                .queryParam("y", latitude)
                .queryParam("radius", radiusM)
                .queryParam("sort", "distance")
                .queryParam("page", 1)
                .queryParam("size", size)
                .build()
                .toUri();

        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "KakaoAK " + restApiKey)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Kakao Local API request failed. status=" + response.statusCode());
            }
            return parseFacilities(response.body(), facilityType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Kakao Local API request interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Kakao Local API request failed.", e);
        }
    }

    private List<KakaoNearbyFacility> parseFacilities(String body, NearbyFacilityType facilityType) throws Exception {
        JsonNode documents = objectMapper.readTree(body).path("documents");
        List<KakaoNearbyFacility> facilities = new ArrayList<>();
        if (!documents.isArray()) {
            return facilities;
        }
        for (JsonNode document : documents) {
            String externalId = text(document, "id");
            String facilityName = text(document, "place_name");
            if (!StringUtils.hasText(externalId) || !StringUtils.hasText(facilityName)) {
                continue;
            }
            facilities.add(new KakaoNearbyFacility(
                    externalId,
                    facilityType,
                    text(document, "category_name"),
                    facilityName,
                    text(document, "phone"),
                    text(document, "address_name"),
                    text(document, "road_address_name"),
                    decimal(document, "y"),
                    decimal(document, "x"),
                    text(document, "place_url"),
                    integer(document, "distance")
            ));
        }
        return facilities;
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private BigDecimal decimal(JsonNode node, String field) {
        String value = text(node, field);
        return value == null ? null : new BigDecimal(value);
    }

    private Integer integer(JsonNode node, String field) {
        String value = text(node, field);
        return value == null ? null : Integer.valueOf(value);
    }
}
