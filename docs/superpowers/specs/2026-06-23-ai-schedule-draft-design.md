# AI Schedule Draft Design

## Goal

Generate an editable trip schedule draft from the authenticated trip's recent chat messages without writing any schedule rows to the database.

## API

`POST /api/trips/{tripId}/ai/schedule-drafts`

Request:

```json
{
  "messageLimit": 100,
  "additionalRequest": "식사 시간을 여유롭게 잡아줘"
}
```

Response:

```json
{
  "summary": "부산 2박 3일 일정 초안",
  "warnings": [],
  "schedules": [
    {
      "placeId": null,
      "scheduleDate": "2026-07-01",
      "startTime": "10:00:00",
      "endTime": "12:00:00",
      "title": "해운대 방문",
      "memo": "채팅 합의를 바탕으로 생성",
      "dayNo": 1,
      "sortOrder": 1
    }
  ]
}
```

The response is only a draft. The frontend lets the user edit it and persists accepted items through the existing `POST /api/trips/{tripId}/schedules` API.

## Architecture

- `AiScheduleDraftController` authenticates the caller and exposes the draft endpoint.
- `AiScheduleDraftService` loads the accessible trip and recent chat messages, builds the model input, invokes the AI client, and validates the returned draft.
- `GmsAiScheduleClient` isolates the GMS OpenAI-compatible HTTP contract and JSON parsing.
- GMS credentials, URL, and model are environment-backed configuration. Credentials are never committed.

## Validation and safety

- The caller must already have access to the trip.
- At least one text chat message is required.
- Message count is limited to 1-100 and defaults to 100.
- AI output must be valid JSON and every item must include a title, date, and start time.
- End time cannot precede start time.
- When trip dates exist, generated schedule dates must stay inside that range.
- The AI must not invent `placeId`; draft items use `null` until a later explicit place-matching feature exists.
- AI/network/malformed-output failures return `502 Bad Gateway`.
- No generated schedules are written by this endpoint.

## Configuration

```properties
ai.gms.api-key=${GMS_KEY:}
ai.gms.chat-completions-url=${GMS_CHAT_COMPLETIONS_URL:https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions}
ai.gms.model=${GMS_MODEL:gpt-4.1-mini}
```

