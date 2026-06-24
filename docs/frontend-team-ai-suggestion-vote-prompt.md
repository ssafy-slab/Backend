# Frontend Implementation Prompt: Team AI Suggestion Voting

아래 요구사항에 맞게 프론트엔드의 AI 일정 제안 UI와 API 연동을 수정해줘.

## 목표

AI 제안은 개인 일정과 팀 일정에서 버튼과 처리 흐름이 다르다.

- 개인 일정(`tripType !== "TEAM"`)
  - `PENDING`: `수락`, `거절` 버튼 표시
  - 수락하면 기존 적용 API를 호출해 즉시 일정에 등록
- 팀 일정(`tripType === "TEAM"`)
  - `PENDING`: `투표 올리기`, `거절` 버튼 표시
  - 직접 수락 버튼은 절대 표시하지 않음
  - 투표 올리기 성공 후 해당 제안은 `VOTING` 상태로 갱신
- 공통
  - `VOTING`: 제안 액션 버튼을 숨기고 `투표 진행 중`과 투표 보기 버튼 표시
  - `APPLIED`: `일정 반영 완료` 표시
  - `REJECTED`: `거절됨` 표시

## API

AI 제안 목록:

```http
GET /api/trips/{tripId}/ai/suggestions?status=PENDING
GET /api/trips/{tripId}/ai/suggestions?status=VOTING
GET /api/trips/{tripId}/ai/suggestions?status=APPLIED
GET /api/trips/{tripId}/ai/suggestions?status=REJECTED
```

제안 응답에는 다음 필드가 추가된다.

```ts
type AiSuggestionStatus = "PENDING" | "VOTING" | "APPLIED" | "REJECTED";

interface AiSuggestion {
  aiSuggestionId: number;
  status: AiSuggestionStatus;
  voteId: number | null;
  appliedScheduleItemId: number | null;
  // 기존 제안 필드는 유지
}
```

개인 일정 수락:

```http
POST /api/trips/{tripId}/ai/suggestions/{suggestionId}/apply
```

팀 일정 투표 생성:

```http
POST /api/trips/{tripId}/ai/suggestions/{suggestionId}/vote
```

요청 body는 없다. 응답은 생성된 `VoteResponse`이며 `찬성`, `반대` 선택지가 포함된다.

제안 거절:

```http
PATCH /api/trips/{tripId}/ai/suggestions/{suggestionId}/reject
```

투표 조회·참여·종료:

```http
GET   /api/trips/{tripId}/votes/{voteId}
PUT   /api/trips/{tripId}/votes/{voteId}/ballot
PATCH /api/trips/{tripId}/votes/{voteId}/close
```

투표 요청:

```json
{
  "voteOptionId": 123
}
```

## 화면 동작

1. 제안 카드 렌더링 시 현재 여행의 `tripType`과 제안의 `status`를 함께 확인한다.
2. 팀 여행의 `PENDING` 카드에서 `투표 올리기`를 누르면 확인창을 띄운다.
3. 생성 중에는 중복 요청 방지를 위해 해당 카드 버튼을 비활성화한다.
4. 성공하면 반환된 `voteId`를 저장하고 제안 목록을 다시 조회하거나 캐시를 `VOTING`으로 갱신한다.
5. `VOTING` 카드의 `투표 보기`는 `/trips/{tripId}/votes/{voteId}` 화면 또는 기존 투표 모달로 연결한다.
6. 투표 종료는 OWNER/EDITOR에게만 노출한다.
7. 종료 후 제안 목록과 일정 목록을 모두 다시 조회한다.
   - 찬성 우세면 제안이 `APPLIED`가 되고 일정이 생성된다.
   - 동률 또는 반대 우세면 제안이 `REJECTED`가 된다.

## 오류 처리

- `400`: 팀 여행에서 직접 수락했거나 개인 여행에서 투표 생성을 시도한 경우
- `403`: 여행 편집 권한 없음
- `409`: 이미 투표가 생성됐거나 제안 상태가 변경됨, 일정 시간 충돌
- `409` 일정 충돌로 투표 종료가 실패하면 투표는 열린 상태이므로 종료 처리 UI를 완료 상태로 바꾸지 않는다.

기존 컴포넌트 구조와 API 클라이언트 패턴을 유지하고, 타입·로딩·오류 상태를 명시적으로 구현해줘.
