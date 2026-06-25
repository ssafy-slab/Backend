package com.ssafy.ssafy_slap.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GmsAiScheduleClientTest {

    @Test
    void sendsBearerRequestAndParsesFencedScheduleJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String draftJson = """
                {
                  "summary": "부산 일정",
                  "warnings": [],
                  "schedules": [{
                    "placeName": "해운대해수욕장",
                    "regionHint": "부산 해운대구",
                    "scheduleDate": "2026-07-01",
                    "startTime": "10:00:00",
                    "endTime": "12:00:00",
                    "title": "해운대",
                    "memo": null,
                    "dayNo": 1,
                    "sortOrder": 1
                  }]
                }
                """;
        String responseBody = objectMapper.writeValueAsString(java.util.Map.of(
                "choices", List.of(java.util.Map.of(
                        "message", java.util.Map.of("content", "```json\n" + draftJson + "\n```")
                ))
        ));
        CapturingHttpClient httpClient = new CapturingHttpClient(responseBody, 200);
        GmsAiScheduleClient client = new GmsAiScheduleClient(
                httpClient,
                objectMapper,
                "test-gms-key",
                "https://gms.example.test/v1/chat/completions",
                "gpt-test"
        );

        var response = client.generate(trip(), List.of(message()), "동선을 줄여줘");

        assertThat(response.summary()).isEqualTo("부산 일정");
        assertThat(response.schedules()).hasSize(1);
        assertThat(response.schedules().get(0).title()).isEqualTo("해운대");
        assertThat(response.schedules().get(0).placeName()).isEqualTo("해운대해수욕장");
        assertThat(response.schedules().get(0).regionHint()).isEqualTo("부산 해운대구");
        assertThat(httpClient.request.headers().firstValue("Authorization"))
                .contains("Bearer test-gms-key");
        assertThat(httpClient.requestBody).contains("\"model\":\"gpt-test\"");
        assertThat(httpClient.requestBody).contains("\"role\":\"developer\"");
        assertThat(httpClient.requestBody).doesNotContain("\"role\":\"system\"");
        assertThat(httpClient.requestBody).doesNotContain("\"temperature\"");
        assertThat(httpClient.requestBody).contains("동선을 줄여줘");
        assertThat(httpClient.requestBody).contains("첫날 오전에는 해운대");
        assertThat(httpClient.requestBody)
                .contains("Keep the JSON field names exactly as specified");
        assertThat(httpClient.requestBody)
                .contains("All user-visible text values must be written in Korean");
        assertThat(httpClient.requestBody).contains("scheduleDate is the start date");
        assertThat(httpClient.requestBody).contains("end through 23:59");
        assertThat(httpClient.requestBody).contains("end by 06:00");
        assertThat(httpClient.requestBody).contains("Use a one hour duration");
        assertThat(httpClient.requestBody).contains("NO_AVAILABLE_SLOT");
    }

    @Test
    void normalizesSingleWarningStringToList() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String draftJson = """
                {
                  "summary": "draft",
                  "warnings": "The exact region is not specified.",
                  "schedules": [{
                    "placeName": "Beach",
                    "regionHint": null,
                    "scheduleDate": "2026-07-01",
                    "startTime": "10:00:00",
                    "endTime": "12:00:00",
                    "title": "Visit beach",
                    "memo": null,
                    "dayNo": 1,
                    "sortOrder": 1
                  }]
                }
                """;
        String responseBody = objectMapper.writeValueAsString(java.util.Map.of(
                "choices", List.of(java.util.Map.of(
                        "message", java.util.Map.of("content", draftJson)
                ))
        ));
        GmsAiScheduleClient client = new GmsAiScheduleClient(
                new CapturingHttpClient(responseBody, 200),
                objectMapper,
                "test-gms-key",
                "https://gms.example.test/v1/chat/completions",
                "gpt-test"
        );

        var response = client.generate(trip(), List.of(message()), null);

        assertThat(response.warnings()).containsExactly("The exact region is not specified.");
    }

    @Test
    void parsesNoResultResponseForInsufficientScheduleContext() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        String draftJson = """
                {
                  "resultStatus": "NO_RESULT",
                  "reasonCode": "NO_SCHEDULE_CONTEXT",
                  "message": "메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다.",
                  "summary": null,
                  "warnings": [],
                  "schedules": []
                }
                """;
        String responseBody = objectMapper.writeValueAsString(java.util.Map.of(
                "choices", List.of(java.util.Map.of(
                        "message", java.util.Map.of("content", draftJson)
                ))
        ));
        GmsAiScheduleClient client = new GmsAiScheduleClient(
                new CapturingHttpClient(responseBody, 200),
                objectMapper,
                "test-gms-key",
                "https://gms.example.test/v1/chat/completions",
                "gpt-test"
        );

        var response = client.generate(trip(), List.of(message()), null);

        assertThat(response.resultStatus()).isEqualTo("NO_RESULT");
        assertThat(response.reasonCode()).isEqualTo("NO_SCHEDULE_CONTEXT");
        assertThat(response.message()).isEqualTo("메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다.");
        assertThat(response.schedules()).isEmpty();
    }

    @Test
    void includesGmsErrorBodyPreviewWhenRequestFails() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        GmsAiScheduleClient client = new GmsAiScheduleClient(
                new CapturingHttpClient("{\"error\":{\"message\":\"unsupported parameter: temperature\"}}", 400),
                objectMapper,
                "test-gms-key",
                "https://gms.example.test/v1/chat/completions",
                "gpt-5.5"
        );

        assertThatThrownBy(() -> client.generate(trip(), List.of(message()), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("GMS request failed with status 400")
                .hasMessageContaining("unsupported parameter: temperature");
    }

    private TripResponse trip() {
        return new TripResponse(
                1L,
                7L,
                "부산 여행",
                null,
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 23, 10, 0),
                LocalDateTime.of(2026, 6, 23, 10, 0)
        );
    }

    private ChatMessageResponse message() {
        return new ChatMessageResponse(
                11L,
                1L,
                7L,
                "여행자",
                "TEXT",
                "첫날 오전에는 해운대",
                LocalDateTime.of(2026, 6, 23, 11, 0)
        );
    }

    private static final class CapturingHttpClient extends HttpClient {
        private final String responseBody;
        private final int statusCode;
        private HttpRequest request;
        private String requestBody;

        private CapturingHttpClient(String responseBody, int statusCode) {
            this.responseBody = responseBody;
            this.statusCode = statusCode;
        }

        @Override
        public <T> HttpResponse<T> send(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) throws IOException, InterruptedException {
            this.request = request;
            this.requestBody = request.bodyPublisher()
                    .map(publisher -> {
                        var subscriber = new BodySubscriber();
                        publisher.subscribe(subscriber);
                        return subscriber.body();
                    })
                    .orElse("");
            @SuppressWarnings("unchecked")
            T typedBody = (T) responseBody;
            return new StubHttpResponse<>(request, typedBody, statusCode);
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
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class BodySubscriber implements java.util.concurrent.Flow.Subscriber<java.nio.ByteBuffer> {
        private final java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();

        @Override
        public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(java.nio.ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            output.writeBytes(bytes);
        }

        @Override
        public void onError(Throwable throwable) {
            throw new IllegalStateException(throwable);
        }

        @Override
        public void onComplete() {
        }

        private String body() {
            return output.toString(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private record StubHttpResponse<T>(
            HttpRequest request,
            T body,
            int statusCode
    ) implements HttpResponse<T> {
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
