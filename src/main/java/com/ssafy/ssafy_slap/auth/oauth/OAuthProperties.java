package com.ssafy.ssafy_slap.auth.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private Provider kakao = new Provider();
    private Provider google = new Provider();
    private Provider naver = new Provider();

    public Provider getKakao() {
        return kakao;
    }

    public void setKakao(Provider kakao) {
        this.kakao = kakao;
    }

    public Provider getGoogle() {
        return google;
    }

    public void setGoogle(Provider google) {
        this.google = google;
    }

    public Provider getNaver() {
        return naver;
    }

    public void setNaver(Provider naver) {
        this.naver = naver;
    }

    public Provider provider(OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> kakao;
            case GOOGLE -> google;
            case NAVER -> naver;
        };
    }

    public String redirectUri(OAuthProvider provider) {
        String configured = provider(provider).getRedirectUri();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "http://localhost:8080/api/oauth/" + provider.path() + "/callback";
    }

    public static class Provider {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
