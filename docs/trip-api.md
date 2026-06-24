# Trip API Reference

This document describes the REST API implemented in the `trip` package.

Source code:
- Controller: `src/main/java/com/ssafy/ssafy_slap/trip/controller/TripController.java`
- Services: `src/main/java/com/ssafy/ssafy_slap/trip/service`
- DTOs: `src/main/java/com/ssafy/ssafy_slap/trip/dto`
- MyBatis XML: `src/main/resources/mapper/trip`
- Schema: `src/main/resources/schema/schema.sql`

All endpoints require an authenticated user. Send the access token with the request, usually as:

```http
Authorization: Bearer <accessToken>
```

Dates use ISO-8601 strings:
- `LocalDate`: `YYYY-MM-DD`
- `LocalTime`: `HH:mm:ss`
- `LocalDateTime`: `YYYY-MM-DDTHH:mm:ss`

## Data Model

The main table is `TRIP`.

| Column | API field | Type | Notes |
|---|---|---|---|
| `trip_id` | `tripId` | `Long` | Generated primary key |
| `owner_user_id` | `ownerUserId` | `Long` | Trip owner |
| `title` | `title` | `String` | Required, max 255 |
| `description` | `description` | `String` | Optional |
| `trip_type` | `tripType` | `String` | Optional, max 50. Existing logic treats `TEAM` specially |
| `start_date` | `startDate` | `LocalDate` | Optional |
| `end_date` | `endDate` | `LocalDate` | Optional, must not be before `startDate` |
| `status` | `status` | `String` | Defaults to `PLANNING` |
| `created_at` | `createdAt` | `LocalDateTime` | DB-generated |
| `updated_at` | `updatedAt` | `LocalDateTime` | DB-updated |

Related tables:
- `TRIP_MEMBER`: stores members and owner membership.
- `TRIP_INVITE_CODE`: stores active invite codes for team trips.
- `SCHEDULE_ITEM`: stores schedule items attached to trips and places.
- `CHECKLIST_ITEM`: stores checklist items attached to trips.

## Endpoint Summary

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/trips` | Create a trip |
| `GET` | `/api/trips` | List trips accessible to the current user |
| `GET` | `/api/trips/{tripId}` | Get one accessible trip |
| `PUT` | `/api/trips/{tripId}` | Update an owned trip |
| `DELETE` | `/api/trips/{tripId}` | Delete an owned trip |
| `POST` | `/api/trips/{tripId}/invite-code` | Create or reuse a team trip invite code |
| `POST` | `/api/trips/join` | Join a team trip with an invite code |
| `GET` | `/api/trips/{tripId}/members` | List trip members |
| `DELETE` | `/api/trips/{tripId}/members/me` | Leave a trip as the current member |
| `PATCH` | `/api/trips/{tripId}/members/{memberUserId}/role` | Update a member role as the trip owner |
| `POST` | `/api/trips/{tripId}/schedules` | Create a schedule item |
| `GET` | `/api/trips/{tripId}/schedules` | List schedule items |
| `PUT` | `/api/trips/{tripId}/schedules/{scheduleItemId}` | Update a schedule item |
| `DELETE` | `/api/trips/{tripId}/schedules/{scheduleItemId}` | Delete a schedule item |
| `POST` | `/api/trips/{tripId}/ai/schedule-drafts` | Analyze new chat and persist schedule suggestions |
| `GET` | `/api/trips/{tripId}/ai/suggestions` | List durable AI suggestions |
| `POST` | `/api/trips/{tripId}/ai/suggestions/{suggestionId}/apply` | Apply one suggestion |
| `PATCH` | `/api/trips/{tripId}/ai/suggestions/{suggestionId}/reject` | Reject one suggestion |
| `POST` | `/api/trips/{tripId}/ai/suggestions/{suggestionId}/vote` | Create an approve/reject vote for one team-trip suggestion |
| `POST` | `/api/trips/{tripId}/ai/analysis-runs/{runId}/apply` | Apply all pending suggestions in a run |
| `PATCH` | `/api/trips/{tripId}/ai/analysis-runs/{runId}/reject` | Reject all pending suggestions in a run |
| `POST` | `/api/trips/{tripId}/checklist-items` | Create a checklist item |
| `GET` | `/api/trips/{tripId}/checklist-items` | List checklist items |
| `DELETE` | `/api/trips/{tripId}/checklist-items/{checklistItemId}` | Delete a checklist item |

## Common Responses

### TripResponse

Used by create, detail, update, and join APIs.

```json
{
  "tripId": 1,
  "ownerUserId": 10,
  "title": "Busan trip",
  "description": "Summer vacation",
  "tripType": "TEAM",
  "startDate": "2026-07-01",
  "endDate": "2026-07-03",
  "status": "PLANNING",
  "createdAt": "2026-06-22T10:00:00",
  "updatedAt": "2026-06-22T10:00:00"
}
```

### TripListResponse

Used by the list API. It contains the same trip fields plus `members`.

```json
{
  "tripId": 1,
  "ownerUserId": 10,
  "title": "Busan trip",
  "description": "Summer vacation",
  "tripType": "TEAM",
  "startDate": "2026-07-01",
  "endDate": "2026-07-03",
  "status": "PLANNING",
  "createdAt": "2026-06-22T10:00:00",
  "updatedAt": "2026-06-22T10:00:00",
  "members": [
    {
      "userId": 10,
      "nickname": "owner",
      "memberRole": "OWNER",
      "inviteStatus": "ACCEPTED",
      "joinedAt": "2026-06-22T10:00:00"
    }
  ]
}
```

## Create Trip

```http
POST /api/trips
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Request body:

```json
{
  "title": "Busan trip",
  "description": "Summer vacation",
  "tripType": "TEAM",
  "startDate": "2026-07-01",
  "endDate": "2026-07-03"
}
```

Field rules:

| Field | Required | Type | Rule |
|---|---:|---|---|
| `title` | Yes | `String` | Not blank, max 255 |
| `description` | No | `String` | Blank text is normalized to `null` |
| `tripType` | No | `String` | Max 50, blank text is normalized to `null` |
| `startDate` | No | `LocalDate` | `YYYY-MM-DD` |
| `endDate` | No | `LocalDate` | Must not be before `startDate` |

Behavior:
- Creates a `TRIP` row with status `PLANNING`.
- Adds the current user to `TRIP_MEMBER` as `OWNER` with `ACCEPTED` status.

Response:
- `201 Created`
- Body: `TripResponse`

## List My Trips

```http
GET /api/trips
Authorization: Bearer <accessToken>
```

Behavior:
- Returns trips where the current user is the owner or an accepted member.
- Includes member information for each trip.
- Sorts by `updated_at DESC`, then `trip_id DESC`.

Response:
- `200 OK`
- Body: array of `TripListResponse`

```json
[
  {
    "tripId": 1,
    "ownerUserId": 10,
    "title": "Busan trip",
    "description": "Summer vacation",
    "tripType": "TEAM",
    "startDate": "2026-07-01",
    "endDate": "2026-07-03",
    "status": "PLANNING",
    "createdAt": "2026-06-22T10:00:00",
    "updatedAt": "2026-06-22T10:00:00",
    "members": [
      {
        "userId": 10,
        "nickname": "owner",
        "memberRole": "OWNER",
        "inviteStatus": "ACCEPTED",
        "joinedAt": "2026-06-22T10:00:00"
      }
    ]
  }
]
```

## Get Trip

```http
GET /api/trips/{tripId}
Authorization: Bearer <accessToken>
```

Path parameters:

| Name | Type | Description |
|---|---|---|
| `tripId` | `Long` | Trip ID |

Behavior:
- Returns the trip only if the current user is the owner or an accepted member.

Response:
- `200 OK`
- Body: `TripResponse`

## Update Trip

```http
PUT /api/trips/{tripId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Only the trip owner can update a trip.

Request body:

```json
{
  "title": "Seoul trip",
  "description": "Autumn plan",
  "tripType": "PERSONAL",
  "startDate": "2026-10-01",
  "endDate": "2026-10-05"
}
```

Field rules are the same as `POST /api/trips`.

Updated columns:
- `title`
- `description`
- `trip_type`
- `start_date`
- `end_date`

Not updated by this API:
- `owner_user_id`
- `status`
- `created_at`
- `updated_at` directly. The database updates `updated_at` automatically.

Response:
- `200 OK`
- Body: `TripResponse`

## Delete Trip

```http
DELETE /api/trips/{tripId}
Authorization: Bearer <accessToken>
```

Only the trip owner can delete a trip.

Response:
- `204 No Content`

Database note:
- Child rows in `TRIP_MEMBER`, `TRIP_INVITE_CODE`, and `SCHEDULE_ITEM` are deleted through foreign-key cascade rules.

## Create Invite Code

```http
POST /api/trips/{tripId}/invite-code
Authorization: Bearer <accessToken>
```

Only the trip owner can create an invite code.

The trip must have `tripType` equal to `TEAM`, case-insensitive.

Behavior:
- If an active invite code already exists for the trip, returns the existing code.
- Otherwise, generates an 8-character code.
- Code alphabet excludes ambiguous characters such as `I`, `O`, `0`, and `1`.

Response:
- `200 OK`

```json
{
  "tripId": 1,
  "inviteCode": "ABCD1234"
}
```

## Join Trip

```http
POST /api/trips/join
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Request body:

```json
{
  "inviteCode": "ABCD1234"
}
```

Field rules:

| Field | Required | Type | Rule |
|---|---:|---|---|
| `inviteCode` | Yes | `String` | Not blank, max 20 |

Behavior:
- Trims and uppercases the invite code.
- Finds an active invite code.
- The target trip must be a `TEAM` trip.
- If the current user is not already a member, inserts a `TRIP_MEMBER` row as `EDITOR` with `ACCEPTED` status.
- If the current user is already a member, returns the trip without inserting another member row.

Response:
- `200 OK`
- Body: `TripResponse`

## List Trip Members

```http
GET /api/trips/{tripId}/members
Authorization: Bearer <accessToken>
```

The current user must be the trip owner or an accepted member.

Response:
- `200 OK`
- Body: array of `TripMemberResponse`

```json
[
  {
    "userId": 10,
    "nickname": "owner",
    "memberRole": "OWNER",
    "inviteStatus": "ACCEPTED",
    "joinedAt": "2026-06-22T10:00:00"
  },
  {
    "userId": 20,
    "nickname": "member",
    "memberRole": "EDITOR",
    "inviteStatus": "ACCEPTED",
    "joinedAt": "2026-06-22T11:00:00"
  }
]
```

Ordering:
- Owner first.
- Then by `joined_at ASC`.
- Then by `trip_member_id ASC`.

## Leave Trip

```http
DELETE /api/trips/{tripId}/members/me
Authorization: Bearer <accessToken>
```

The current user leaves the trip by deleting their `TRIP_MEMBER` row.

Rules:
- A normal member can leave.
- The owner cannot leave with this API. The owner should delete the trip instead.

Response:
- `204 No Content`

## Update Member Role

```http
PATCH /api/trips/{tripId}/members/{memberUserId}/role
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Only the trip owner can update a member role.

Path parameters:

| Name | Type | Description |
|---|---|---|
| `tripId` | `Long` | Trip ID |
| `memberUserId` | `Long` | User ID of the member to update |

Request body:

```json
{
  "memberRole": "VIEWER"
}
```

Allowed `memberRole` values:

| Value | Meaning |
|---|---|
| `EDITOR` | Can view the trip and edit schedules |
| `VIEWER` | Can view the trip but cannot edit schedules |

Rules:
- The requester must be the trip owner.
- The owner role cannot be changed with this API.
- The target user must be a member of the trip.
- `OWNER` is not accepted in this API.

Response:
- `200 OK`
- Body: `TripMemberResponse`

```json
{
  "userId": 20,
  "nickname": "member",
  "memberRole": "VIEWER",
  "inviteStatus": "ACCEPTED",
  "joinedAt": "2026-06-22T11:00:00"
}
```

## Create Schedule Item

```http
POST /api/trips/{tripId}/schedules
Authorization: Bearer <accessToken>
Content-Type: application/json
```

The current user must be the trip owner or an accepted member with `EDITOR` role.

Request body:

```json
{
  "placeId": 100,
  "scheduleDate": "2026-07-01",
  "startTime": "10:00:00",
  "endTime": "11:30:00",
  "title": "Beach walk",
  "memo": "Bring water",
  "dayNo": 1,
  "sortOrder": 1
}
```

Field rules:

| Field | Required | Type | Rule |
|---|---:|---|---|
| `placeId` | No | `Long` | If present, must exist in `PLACE`; omit for a free-form schedule |
| `scheduleDate` | Yes | `LocalDate` | `YYYY-MM-DD` |
| `startTime` | Yes | `LocalTime` | `HH:mm:ss` |
| `endTime` | No | `LocalTime` | Must not be before `startTime` |
| `title` | No | `String` | Max 255, blank text is normalized to `null` |
| `memo` | No | `String` | Blank text is normalized to `null` |
| `dayNo` | No | `Integer` | Optional day number |
| `sortOrder` | No | `Integer` | Optional display order |

Behavior:
- Validates trip edit permission.
- Validates that `placeId` exists when provided.
- Inserts a row into `SCHEDULE_ITEM`.
- The database prevents duplicate schedules with the same `tripId`, `scheduleDate`, and `startTime`.

Response:
- `201 Created`

```json
{
  "scheduleItemId": 99,
  "tripId": 1,
  "placeId": 100,
  "createdByUserId": 10,
  "dayNo": 1,
  "scheduleDate": "2026-07-01",
  "startTime": "10:00:00",
  "endTime": "11:30:00",
  "title": "Beach walk",
  "memo": "Bring water",
  "sortOrder": 1,
  "createdAt": "2026-06-22T10:00:00",
  "updatedAt": "2026-06-22T10:00:00"
}
```

## Generate AI Schedule Draft

```http
POST /api/trips/{tripId}/ai/schedule-drafts
Authorization: Bearer <accessToken>
Content-Type: application/json
```

This button endpoint analyzes text messages that have not been analyzed yet, stores an `AI_ANALYSIS_RUN`, and stores each generated item in `AI_SUGGESTION`. It does not create `SCHEDULE_ITEM` rows until the user applies a suggestion.

The analysis input also includes every existing schedule item in the trip. When chat context does not
specify a date or time, the AI chooses a non-overlapping slot within the trip period:

- Available time is `07:00` inclusive through `23:00` exclusive.
- Default duration is one hour.
- Existing schedules and suggestions generated in the same response cannot overlap.
- With no contextual preference, the earliest available slot is used.
- When no one-hour slot is available, analysis returns `NO_RESULT` with no suggestions.
- The backend validates date range, available hours, and overlaps before storing suggestions.

Request body:

```json
{
  "messageLimit": 100,
  "additionalRequest": "Keep meal times relaxed"
}
```

`messageLimit` defaults to `100` and must be between `1` and `100`. The body itself may be omitted.

Response:

```json
{
  "analysisRunId": 12,
  "triggerType": "BUTTON",
  "status": "SUCCEEDED",
  "suggestions": [
    {
      "aiSuggestionId": 31,
      "analysisRunId": 12,
      "tripId": 1,
      "suggestedPlaceId": 351,
      "suggestedPlaceName": "해운대해수욕장",
      "suggestedRegionHint": "부산 해운대구",
      "title": "해운대 방문",
      "summary": "단체 채팅에서 생성된 일정",
      "reason": "여행 채팅을 기반으로 생성됨",
      "scheduleDate": "2026-07-01",
      "startTime": "10:00:00",
      "endTime": "12:00:00",
      "dayNo": 1,
      "sortOrder": 1,
      "status": "PENDING",
      "appliedScheduleItemId": null
    }
  ]
}
```

The AI returns `placeName` and `regionHint`, never an internal database ID. Before persistence, the backend searches `PLACE` by an exact normalized place name and uses the region hint to disambiguate candidates. `suggestedPlaceId` is populated only when one verified candidate remains. The original AI text is preserved in `suggestedPlaceName` and `suggestedRegionHint`.

If the place is missing or ambiguous, `suggestedPlaceId` remains `null`. Free-form schedules are supported because `SCHEDULE_ITEM.place_id` is nullable.

Automatic analysis uses the same storage flow. After each committed text message, the server counts messages after `AI_ANALYSIS_STATE.last_analyzed_message_id`. At 30 messages it starts one asynchronous `AUTO` run. `AI_ANALYSIS_STATE.analysis_status` prevents overlapping runs.

After a successful run, the chat WebSocket broadcasts:

```json
{
  "type": "AI_ANALYSIS_COMPLETED",
  "tripId": 1,
  "analysisRunId": 12
}
```

If the AI determines that the messages are too sparse or contain no schedule-related context,
the analysis is completed without creating `AI_SUGGESTION` rows. The analyzed-message cursor
still advances so the same messages are not retried indefinitely. The button response has
`status: "NO_RESULT"` and an empty `suggestions` array, and the chat WebSocket broadcasts:

```json
{
  "type": "AI_ANALYSIS_NO_RESULT",
  "tripId": 1,
  "analysisRunId": 12,
  "reasonCode": "NO_SCHEDULE_CONTEXT",
  "message": "메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다."
}
```

Possible no-result reason codes are `INSUFFICIENT_MESSAGES` and `NO_SCHEDULE_CONTEXT`.
`AI_ANALYSIS_NO_RESULT` represents a valid analysis outcome, not an API or infrastructure
failure. Malformed AI responses and external API failures remain failed analyses and use the
existing retry handling.

The frontend should then call:

```http
GET /api/trips/{tripId}/ai/suggestions?status=PENDING
```

It should also call this endpoint when the AI suggestion screen opens. Because the rows are stored in MySQL, suggestions remain available after the frontend is closed or refreshed.

Apply or reject:

```http
POST  /api/trips/{tripId}/ai/suggestions/{suggestionId}/apply
PATCH /api/trips/{tripId}/ai/suggestions/{suggestionId}/reject
POST  /api/trips/{tripId}/ai/analysis-runs/{runId}/apply
PATCH /api/trips/{tripId}/ai/analysis-runs/{runId}/reject
```

Applying creates a `SCHEDULE_ITEM` and changes the suggestion status to `APPLIED`. Rejecting changes it to `REJECTED`. Only `PENDING` suggestions can transition.

For `tripType: "TEAM"`, direct apply is rejected. A pending suggestion can instead be submitted to a vote:

```http
POST /api/trips/{tripId}/ai/suggestions/{suggestionId}/vote
```

This creates one vote with `찬성` and `반대` options and changes the suggestion status to `VOTING`.
The suggestion response includes `voteId`.
The vote response exposes `eligibleVoterCount`, `votedMemberCount`, and `allMembersVoted`.
Closing an AI suggestion vote returns `409 Conflict` until every accepted trip member has voted.
Closing the vote applies the suggestion only when approval
has more ballots than rejection; ties and rejection majorities mark it `REJECTED`.

Configuration:

```properties
GMS_KEY=<secret>
GMS_CHAT_COMPLETIONS_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions
GMS_MODEL=gpt-4.1-mini
```

Error cases:

- `400 Bad Request`: no usable chat messages or an invalid message limit.
- `401 Unauthorized`: authentication is missing.
- `404 Not Found`: the trip is not accessible.
- `502 Bad Gateway`: GMS failed or returned an invalid draft.
- `503 Service Unavailable`: `GMS_KEY` is not configured.

## List Schedule Items

```http
GET /api/trips/{tripId}/schedules
Authorization: Bearer <accessToken>
```

The current user must be the trip owner or an accepted member.

Response:
- `200 OK`
- Body: array of `TripScheduleResponse`

Ordering:
- `schedule_date ASC`
- `start_time ASC`
- `sort_order ASC`
- `schedule_item_id ASC`

```json
[
  {
    "scheduleItemId": 99,
    "tripId": 1,
    "placeId": 100,
    "createdByUserId": 10,
    "dayNo": 1,
    "scheduleDate": "2026-07-01",
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "title": "Beach walk",
    "memo": "Bring water",
    "sortOrder": 1,
    "createdAt": "2026-06-22T10:00:00",
    "updatedAt": "2026-06-22T10:00:00"
  }
]
```

## Update Schedule Item

```http
PUT /api/trips/{tripId}/schedules/{scheduleItemId}
Authorization: Bearer <accessToken>
Content-Type: application/json
```

The current user must be the trip owner or an accepted member with `EDITOR` role.

Request body:

```json
{
  "placeId": 101,
  "scheduleDate": "2026-07-02",
  "startTime": "13:00:00",
  "endTime": "14:30:00",
  "title": "Lunch",
  "memo": "Seafood",
  "dayNo": 2,
  "sortOrder": 3
}
```

Field rules are the same as `POST /api/trips/{tripId}/schedules`.

The `placeId` field can be `null` to make the schedule free-form instead of place-based.

Updated columns:
- `place_id`
- `created_by_user_id`
- `day_no`
- `schedule_date`
- `start_time`
- `end_time`
- `title`
- `memo`
- `sort_order`

Response:
- `200 OK`
- Body: `TripScheduleResponse`

## Delete Schedule Item

```http
DELETE /api/trips/{tripId}/schedules/{scheduleItemId}
Authorization: Bearer <accessToken>
```

The current user must be the trip owner or an accepted member with `EDITOR` role.

Path parameters:

| Name | Type | Description |
|---|---|---|
| `tripId` | `Long` | Trip ID |
| `scheduleItemId` | `Long` | Schedule item ID |

Response:
- `204 No Content`

## Create Checklist Item

```http
POST /api/trips/{tripId}/checklist-items
Authorization: Bearer <accessToken>
Content-Type: application/json
```

The current user must be the trip owner or an accepted member with `EDITOR` role.

Request body:

```json
{
  "title": "Pack passport",
  "assigneeUserId": 20,
  "dueAt": "2026-07-01T09:00:00"
}
```

Field rules:

| Field | Required | Type | Rule |
|---|---:|---|---|
| `title` | Yes | `String` | Not blank, max 255, trimmed before saving |
| `assigneeUserId` | No | `Long` | Must be the trip owner or an accepted trip member |
| `dueAt` | No | `LocalDateTime` | Optional deadline |

Response:
- `201 Created`

```json
{
  "checklistItemId": 99,
  "tripId": 1,
  "assigneeUserId": 20,
  "title": "Pack passport",
  "done": false,
  "dueAt": "2026-07-01T09:00:00",
  "createdAt": "2026-06-23T10:00:00",
  "completedAt": null
}
```

## List Checklist Items

```http
GET /api/trips/{tripId}/checklist-items
Authorization: Bearer <accessToken>
```

The current user must be the trip owner or an accepted member.

Response:
- `200 OK`
- Body: array of `ChecklistItemResponse`

Ordering:
- Incomplete items first.
- Items with a due date before items without one.
- Earlier due dates first.
- Then by `checklist_item_id ASC`.

## Delete Checklist Item

```http
DELETE /api/trips/{tripId}/checklist-items/{checklistItemId}
Authorization: Bearer <accessToken>
```

The current user must be the trip owner or an accepted member with `EDITOR` role.

Response:
- `204 No Content`

## Error Cases

The services throw `ResponseStatusException`. Spring converts these into HTTP error responses.

| Status | Typical reason |
|---|---|
| `400 Bad Request` | Missing body, blank required field, invalid date/time range, invite code requested for non-team trip, invalid member role |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | User cannot access the trip, or can access it but is not allowed to perform owner-only action |
| `404 Not Found` | Trip, invite code, place, schedule item, or checklist item was not found |
| `409 Conflict` | Invite code generation failed after repeated collisions |

## Member Role Storage

The database can store trip member authority.

`TRIP_MEMBER.member_role` is a `VARCHAR(50)` column with default value `MEMBER`. Current code writes:
- `OWNER` when a trip is created.
- `EDITOR` when a user joins by invite code.
- `EDITOR` or `VIEWER` when the owner updates member role.

This means role storage is already available. The database does not currently enforce an enum or check constraint, so valid role values must be controlled by application code.

## Curl Examples

Create a team trip:

```bash
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Busan trip",
    "description": "Summer vacation",
    "tripType": "TEAM",
    "startDate": "2026-07-01",
    "endDate": "2026-07-03"
  }'
```

Update a trip:

```bash
curl -X PUT http://localhost:8080/api/trips/1 \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Seoul trip",
    "description": "Autumn plan",
    "tripType": "PERSONAL",
    "startDate": "2026-10-01",
    "endDate": "2026-10-05"
  }'
```

Create an invite code:

```bash
curl -X POST http://localhost:8080/api/trips/1/invite-code \
  -H "Authorization: Bearer <accessToken>"
```

Join with an invite code:

```bash
curl -X POST http://localhost:8080/api/trips/join \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"inviteCode":"ABCD1234"}'
```

List trip members:

```bash
curl http://localhost:8080/api/trips/1/members \
  -H "Authorization: Bearer <accessToken>"
```

Leave a trip:

```bash
curl -X DELETE http://localhost:8080/api/trips/1/members/me \
  -H "Authorization: Bearer <accessToken>"
```

Update a member role:

```bash
curl -X PATCH http://localhost:8080/api/trips/1/members/20/role \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{"memberRole":"VIEWER"}'
```

Create a schedule item:

```bash
curl -X POST http://localhost:8080/api/trips/1/schedules \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "placeId": 100,
    "scheduleDate": "2026-07-01",
    "startTime": "10:00:00",
    "endTime": "11:30:00",
    "title": "Beach walk",
    "memo": "Bring water",
    "dayNo": 1,
    "sortOrder": 1
  }'
```
