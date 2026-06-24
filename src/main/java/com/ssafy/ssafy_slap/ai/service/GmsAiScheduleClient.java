package com.ssafy.ssafy_slap.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GmsAiScheduleClient implements AiScheduleClient {

    private static final String SYSTEM_PROMPT = """
            You generate an editable travel schedule draft from a trip chat.
            Return only one JSON object with fields:
            resultStatus, reasonCode, message, summary, warnings, and schedules.
            Keep the JSON field names exactly as specified in English so clients can parse them.
            All user-visible text values must be written in Korean:
            summary, each warnings entry, placeName, regionHint, title, and memo.
            Do not use English action phrases such as "Visit" or "Spend 2 hours at".
            Preserve a foreign proper noun only when no commonly used Korean name exists.
            If schedule suggestions can be created, set resultStatus to "SUCCESS",
            set reasonCode and message to null, and return one or more schedules.
            If the chat has too little information or no schedule-related decisions,
            set resultStatus to "NO_RESULT", set reasonCode to "INSUFFICIENT_MESSAGES"
            or "NO_SCHEDULE_CONTEXT", write a clear Korean message for the user,
            and return an empty schedules array. Do not invent suggestions just to avoid NO_RESULT.
            warnings must always be a JSON array of strings. Use [] when there are no warnings.
            Each schedules item must contain:
            placeName, regionHint, scheduleDate, startTime, endTime, title, memo, dayNo, sortOrder.
            placeName is the specific venue name supported by the chat, or null.
            regionHint is a city, district, or address hint supported by the chat, or null.
            Never invent or return an internal database ID.
            Use ISO dates (yyyy-MM-dd) and times (HH:mm:ss).
            Existing schedules are occupied and must never overlap a suggestion.
            If the chat omits a date or time, choose an available slot inside the trip period.
            Available hours are 07:00 inclusive through 23:00 exclusive.
            Use a one hour duration when the chat does not specify duration.
            Suggestions in the same response must not overlap each other.
            When context gives no time preference, choose the earliest available slot.
            If no free one-hour slot exists, return NO_RESULT with reasonCode "NO_AVAILABLE_SLOT".
            Do not include markdown fences or explanatory text.
            Use only decisions supported by the chat. Put uncertainty in warnings.
            """;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String chatCompletionsUrl;
    private final String model;

    @Autowired
    public GmsAiScheduleClient(
            @Value("${ai.gms.api-key:}") String apiKey,
            @Value("${ai.gms.chat-completions-url:https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions}")
            String chatCompletionsUrl,
            @Value("${ai.gms.model:gpt-4.1-mini}") String model
    ) {
        this(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                new ObjectMapper().findAndRegisterModules(),
                apiKey,
                chatCompletionsUrl,
                model
        );
    }

    GmsAiScheduleClient(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiKey,
            String chatCompletionsUrl,
            String model
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.chatCompletionsUrl = chatCompletionsUrl;
        this.model = model;
    }

    @Override
    public AiScheduleDraftResponse generate(
            TripResponse trip,
            List<ChatMessageResponse> messages,
            List<TripScheduleItem> existingSchedules,
            String additionalRequest
    ) {
        if (!StringUtils.hasText(apiKey)) {
            log.error("GMS AI request blocked: apiKeyConfigured=false, urlConfigured={}, modelConfigured={}",
                    StringUtils.hasText(chatCompletionsUrl), StringUtils.hasText(model));
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "GMS API key is not configured");
        }

        try {
            log.info("GMS AI request started: tripId={}, messageCount={}, apiKeyConfigured=true, urlConfigured={}, model={}",
                    trip.tripId(), messages.size(), StringUtils.hasText(chatCompletionsUrl), model);
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content",
                                    userPrompt(trip, messages, existingSchedules, additionalRequest))
                    )
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(chatCompletionsUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("GMS AI request failed: tripId={}, status={}, responseBodyPreview={}",
                        trip.tripId(), response.statusCode(), preview(response.body()));
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "GMS request failed with status " + response.statusCode()
                );
            }
            log.info("GMS AI request succeeded: tripId={}, status={}", trip.tripId(), response.statusCode());
            return parseResponse(response.body());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("GMS AI request interrupted: tripId={}", trip.tripId(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GMS request was interrupted", exception);
        } catch (Exception exception) {
            log.error("GMS AI request failed unexpectedly: tripId={}", trip.tripId(), exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to generate AI schedule draft", exception);
        }
    }

    private String preview(String body) {
        if (body == null) {
            return null;
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }

    private AiScheduleDraftResponse parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String content = root.path("choices").path(0).path("message").path("content").asText(null);
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("GMS response content is missing");
        }
        return objectMapper.readValue(stripMarkdownFence(content), AiScheduleDraftResponse.class);
    }

    private String stripMarkdownFence(String content) {
        String normalized = content.trim();
        if (!normalized.startsWith("```")) {
            return normalized;
        }
        int firstLineEnd = normalized.indexOf('\n');
        int closingFence = normalized.lastIndexOf("```");
        if (firstLineEnd < 0 || closingFence <= firstLineEnd) {
            return normalized;
        }
        return normalized.substring(firstLineEnd + 1, closingFence).trim();
    }

    String userPrompt(
            TripResponse trip,
            List<ChatMessageResponse> messages,
            List<TripScheduleItem> existingSchedules,
            String additionalRequest
    ) {
        StringBuilder prompt = new StringBuilder()
                .append("Trip title: ").append(trip.title()).append('\n')
                .append("Trip start date: ").append(trip.startDate()).append('\n')
                .append("Trip end date: ").append(trip.endDate()).append('\n');
        if (StringUtils.hasText(additionalRequest)) {
            prompt.append("Additional request: ").append(additionalRequest).append('\n');
        }
        prompt.append("Existing schedules (occupied time; do not overlap):\n");
        if (existingSchedules == null || existingSchedules.isEmpty()) {
            prompt.append("- none\n");
        } else {
            for (TripScheduleItem schedule : existingSchedules) {
                LocalTime endTime = schedule.getEndTime() == null
                        ? schedule.getStartTime().plusHours(1)
                        : schedule.getEndTime();
                prompt.append("- ")
                        .append(schedule.getScheduleDate()).append(' ')
                        .append(schedule.getStartTime()).append('-').append(endTime)
                        .append(" | ").append(schedule.getTitle())
                        .append('\n');
            }
        }
        prompt.append("Chat messages in chronological order:\n");
        for (ChatMessageResponse message : messages) {
            prompt.append('[')
                    .append(message.createdAt())
                    .append("] ")
                    .append(message.senderNickname())
                    .append(": ")
                    .append(message.content())
                    .append('\n');
        }
        return prompt.toString();
    }
}
