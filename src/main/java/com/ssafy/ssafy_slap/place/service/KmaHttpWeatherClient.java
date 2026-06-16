package com.ssafy.ssafy_slap.place.service;

import com.ssafy.ssafy_slap.place.dto.PlaceWeatherForecast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KmaHttpWeatherClient implements KmaWeatherClient {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final int[] BASE_TIMES = {200, 500, 800, 1100, 1400, 1700, 2000, 2300};
    private static final int FORECAST_DAYS = 4;
    private static final Pattern ITEM_PATTERN = Pattern.compile("\\{[^{}]*\"baseDate\"[^{}]*}");
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"?([^\",}]*)\"?");

    private final HttpClient httpClient;
    private final String serviceKey;
    private final String apiUrl;

    @Autowired
    public KmaHttpWeatherClient(
            @Value("${weather.kma.service-key:}") String serviceKey,
            @Value("${weather.kma.vilage-forecast-url:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst}") String apiUrl
    ) {
        this(HttpClient.newHttpClient(), serviceKey, apiUrl);
    }

    KmaHttpWeatherClient(HttpClient httpClient, String serviceKey, String apiUrl) {
        this.httpClient = httpClient;
        this.serviceKey = serviceKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public Optional<List<PlaceWeatherForecast>> fetchForecasts(KmaGridCoordinate coordinate, LocalDateTime now) {
        if (!StringUtils.hasText(serviceKey)) {
            return Optional.empty();
        }

        try {
            BaseDateTime baseDateTime = resolveBaseDateTime(now);
            URI uri = URI.create(apiUrl + "?"
                    + "serviceKey=" + serviceKey
                    + "&pageNo=1"
                    + "&numOfRows=2000"
                    + "&dataType=JSON"
                    + "&base_date=" + baseDateTime.date().format(DATE_FORMATTER)
                    + "&base_time=" + baseDateTime.time()
                    + "&nx=" + coordinate.nx()
                    + "&ny=" + coordinate.ny());
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            return parseForecasts(response.body(), now, baseDateTime);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<List<PlaceWeatherForecast>> parseForecasts(String body, LocalDateTime now, BaseDateTime baseDateTime) {
        List<ForecastItem> items = parseItems(body);
        if (items.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Map<String, String>> groupedValues = new LinkedHashMap<>();
        for (ForecastItem item : items) {
            String forecastDateTime = item.fcstDate() + item.fcstTime();
            groupedValues.computeIfAbsent(forecastDateTime, ignored -> new HashMap<>())
                    .put(item.category(), item.fcstValue());
        }

        String nowKey = now.format(DATE_TIME_FORMATTER);
        String endDateKey = now.toLocalDate().plusDays(FORECAST_DAYS - 1).format(DATE_FORMATTER);
        List<PlaceWeatherForecast> forecasts = new ArrayList<>();
        groupedValues.entrySet().stream()
                .filter(entry -> entry.getKey().compareTo(nowKey) >= 0)
                .filter(entry -> entry.getKey().substring(0, 8).compareTo(endDateKey) <= 0)
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> forecasts.add(toForecast(entry.getKey(), entry.getValue(), baseDateTime)));

        if (forecasts.isEmpty()) {
            groupedValues.entrySet().stream()
                    .filter(entry -> entry.getKey().substring(0, 8).compareTo(endDateKey) <= 0)
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> forecasts.add(toForecast(entry.getKey(), entry.getValue(), baseDateTime)));
        }

        return forecasts.isEmpty() ? Optional.empty() : Optional.of(forecasts);
    }

    private List<ForecastItem> parseItems(String body) {
        return ITEM_PATTERN.matcher(body).results()
                .map(match -> toForecastItem(match.group()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<ForecastItem> toForecastItem(String itemJson) {
        Map<String, String> fields = new HashMap<>();
        Matcher matcher = FIELD_PATTERN.matcher(itemJson);
        while (matcher.find()) {
            fields.put(matcher.group(1), matcher.group(2));
        }
        String category = fields.get("category");
        String fcstDate = fields.get("fcstDate");
        String fcstTime = fields.get("fcstTime");
        String fcstValue = fields.get("fcstValue");
        if (!StringUtils.hasText(category) || !StringUtils.hasText(fcstDate) || !StringUtils.hasText(fcstTime)) {
            return Optional.empty();
        }
        return Optional.of(new ForecastItem(category, fcstDate, fcstTime, fcstValue));
    }

    private PlaceWeatherForecast toForecast(String forecastDateTime, Map<String, String> values, BaseDateTime baseDateTime) {
        return new PlaceWeatherForecast(
                toBigDecimal(values.get("TMP")),
                toInteger(values.get("POP")),
                toInteger(values.get("REH")),
                toBigDecimal(values.get("WSD")),
                precipitationType(values.get("PTY")),
                skyStatus(values.get("SKY")),
                emptyToNull(values.get("PCP")),
                LocalDateTime.parse(forecastDateTime, DATE_TIME_FORMATTER),
                LocalDateTime.of(baseDateTime.date(), baseDateTime.localTime())
        );
    }

    private BaseDateTime resolveBaseDateTime(LocalDateTime now) {
        LocalDateTime safeNow = now.minusMinutes(20);
        int current = safeNow.getHour() * 100;
        for (int i = BASE_TIMES.length - 1; i >= 0; i--) {
            if (current >= BASE_TIMES[i]) {
                return new BaseDateTime(safeNow.toLocalDate(), String.format("%04d", BASE_TIMES[i]));
            }
        }
        return new BaseDateTime(safeNow.toLocalDate().minusDays(1), "2300");
    }

    private BigDecimal toBigDecimal(String value) {
        if (!StringUtils.hasText(value) || "\uac15\uc218\uc5c6\uc74c".equals(value)) {
            return null;
        }
        return new BigDecimal(value.replace("mm", "").trim());
    }

    private Integer toInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    private String precipitationType(String value) {
        return switch (value == null ? "" : value) {
            case "0" -> "NONE";
            case "1" -> "RAIN";
            case "2" -> "RAIN_SNOW";
            case "3" -> "SNOW";
            case "4" -> "SHOWER";
            default -> null;
        };
    }

    private String skyStatus(String value) {
        return switch (value == null ? "" : value) {
            case "1" -> "CLEAR";
            case "3" -> "MOSTLY_CLOUDY";
            case "4" -> "CLOUDY";
            default -> null;
        };
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    private record BaseDateTime(LocalDate date, String time) {
        LocalTime localTime() {
            return LocalTime.of(Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(2, 4)));
        }
    }

    private record ForecastItem(String category, String fcstDate, String fcstTime, String fcstValue) {
    }
}
