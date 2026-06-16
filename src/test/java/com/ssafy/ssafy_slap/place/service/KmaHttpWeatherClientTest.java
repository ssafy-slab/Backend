package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class KmaHttpWeatherClientTest {

    @Test
    void keepsForecastsThroughFourCalendarDays() {
        KmaHttpWeatherClient client = new KmaHttpWeatherClient(
                new StubHttpClient(kmaResponseBody(LocalDate.of(2026, 6, 15))),
                "service-key",
                "http://weather.example.test/forecast"
        );

        Optional<List<PlaceWeatherForecast>> result = client.fetchForecasts(
                new KmaGridCoordinate(60, 127),
                LocalDateTime.of(2026, 6, 15, 20, 43)
        );

        assertThat(result).isPresent();
        assertThat(result.get())
                .extracting(forecast -> forecast.forecastAt().toLocalDate())
                .contains(LocalDate.of(2026, 6, 18))
                .doesNotContain(LocalDate.of(2026, 6, 19));
    }

    private static String kmaResponseBody(LocalDate startDate) {
        StringBuilder body = new StringBuilder("""
                {"response":{"header":{"resultCode":"00"},"body":{"items":{"item":[
                """);

        boolean first = true;
        for (int day = 0; day < 5; day++) {
            LocalDate date = startDate.plusDays(day);
            for (int hour = 0; hour < 24; hour++) {
                if (!first) {
                    body.append(',');
                }
                first = false;
                body.append("""
                        {"baseDate":"20260615","baseTime":"2000","category":"TMP","fcstDate":"%s","fcstTime":"%02d00","fcstValue":"22"}
                        """.formatted(date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), hour));
            }
        }

        body.append("""
                ]}}}}
                """);
        return body.toString();
    }

    private static final class StubHttpClient extends HttpClient {
        private final String body;

        private StubHttpClient(String body) {
            this.body = body;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            @SuppressWarnings("unchecked")
            T typedBody = (T) body;
            return new StubHttpResponse<>(request, typedBody);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
    }

    private record StubHttpResponse<T>(HttpRequest request, T body) implements HttpResponse<T> {
        @Override
        public int statusCode() {
            return 200;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (left, right) -> true);
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }
    }
}
