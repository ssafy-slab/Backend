package com.ssafy.ssafy_slap.auth.oauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class RestOAuthClient implements OAuthClient {

    private final RestClient restClient;
    private final OAuthProperties properties;

    public RestOAuthClient(OAuthProperties properties) {
        this.restClient = RestClient.create();
        this.properties = properties;
    }

    @Override
    public OAuthTokenResponse exchangeCode(OAuthProvider provider, String code, String state) {
        Map<String, Object> response = provider == OAuthProvider.NAVER
                ? exchangeNaverCode(provider, code, state)
                : exchangeFormCode(provider, code);

        String accessToken = stringAt(response, "access_token");
        if (accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OAuth token response did not include access token");
        }

        return new OAuthTokenResponse(accessToken);
    }

    @Override
    public OAuthProviderProfile fetchProfile(OAuthProvider provider, String accessToken) {
        Map<String, Object> response = restClient.get()
                .uri(provider.profileUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            return new OAuthProviderProfile("", "", "");
        }

        return switch (provider) {
            case KAKAO -> kakaoProfile(response);
            case GOOGLE -> googleProfile(response);
            case NAVER -> naverProfile(response);
        };
    }

    private Map<String, Object> exchangeFormCode(OAuthProvider provider, String code) {
        OAuthProperties.Provider providerProperties = properties.provider(provider);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", providerProperties.getClientId());
        form.add("redirect_uri", properties.redirectUri(provider));
        form.add("code", code);
        if (providerProperties.getClientSecret() != null && !providerProperties.getClientSecret().isBlank()) {
            form.add("client_secret", providerProperties.getClientSecret());
        }

        return restClient.post()
                .uri(provider.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);
    }

    private Map<String, Object> exchangeNaverCode(OAuthProvider provider, String code, String state) {
        OAuthProperties.Provider providerProperties = properties.provider(provider);
        String uri = UriComponentsBuilder.fromUriString(provider.tokenUrl())
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", providerProperties.getClientId())
                .queryParam("client_secret", providerProperties.getClientSecret())
                .queryParam("redirect_uri", properties.redirectUri(provider))
                .queryParam("code", code)
                .queryParam("state", state)
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(Map.class);
    }

    private OAuthProviderProfile kakaoProfile(Map<String, Object> response) {
        Map<String, Object> account = mapAt(response, "kakao_account");
        Map<String, Object> profile = mapAt(account, "profile");
        Map<String, Object> properties = mapAt(response, "properties");
        String nickname = firstNonBlank(stringAt(profile, "nickname"), stringAt(properties, "nickname"));
        return new OAuthProviderProfile(
                stringAt(response, "id"),
                stringAt(account, "email"),
                nickname
        );
    }

    private OAuthProviderProfile googleProfile(Map<String, Object> response) {
        return new OAuthProviderProfile(
                stringAt(response, "sub"),
                stringAt(response, "email"),
                stringAt(response, "name")
        );
    }

    private OAuthProviderProfile naverProfile(Map<String, Object> response) {
        Map<String, Object> profile = mapAt(response, "response");
        return new OAuthProviderProfile(
                stringAt(profile, "id"),
                stringAt(profile, "email"),
                firstNonBlank(stringAt(profile, "nickname"), stringAt(profile, "name"))
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapAt(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String stringAt(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
