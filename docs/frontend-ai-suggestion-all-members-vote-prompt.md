# 프론트엔드 구현 프롬프트: 팀 AI 제안 전원 투표 후 종료

팀 여행의 AI 일정 제안 투표가 모든 팀원의 투표가 완료된 뒤에만 종료되도록 프론트엔드를 수정해줘.

## 백엔드 계약

투표 조회와 투표 제출 응답인 `VoteResponse`에 다음 필드가 추가되었다.

```ts
interface VoteResponse {
  voteId: number;
  status: "OPEN" | "CLOSED";
  options: VoteOptionResponse[];
  selectedOptionId: number | null;
  totalBallotCount: number;
  eligibleVoterCount: number;
  votedMemberCount: number;
  allMembersVoted: boolean;
}
```

- `eligibleVoterCount`: 현재 여행의 `ACCEPTED` 팀원 수
- `votedMemberCount`: 현재 투표에 참여한 `ACCEPTED` 팀원 수
- `allMembersVoted`: 전원이 투표했으면 `true`
- 팀 AI 제안과 연결된 투표는 `allMembersVoted === false`인 상태에서 종료 API를 호출하면 `409 Conflict`와 `All trip members must vote before closing` 메시지를 반환한다.
- 일반 투표의 기존 종료 정책은 유지된다.

사용 API:

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

## 화면 요구사항

1. 팀 AI 제안 투표 화면에 `투표 완료 {votedMemberCount}/{eligibleVoterCount}`를 표시한다.
2. `allMembersVoted === false`이면 종료 버튼을 비활성화하고 `모든 팀원이 투표하면 종료됩니다` 안내를 표시한다.
3. 사용자가 투표를 제출한 뒤 `PUT /ballot` 응답을 기준으로 화면 상태를 즉시 갱신한다.
4. 투표 제출 응답이 `status === "OPEN"`이고 `allMembersVoted === true`이면 종료 권한이 있는 사용자(OWNER/EDITOR)는 `PATCH /close`를 자동 호출한다.
5. 자동 종료 요청 중에는 투표 선택과 종료 액션을 중복 실행할 수 없도록 막는다.
6. 종료 성공 후 투표 상세, AI 제안 목록, 일정 목록을 다시 조회한다.
   - 찬성 우세: AI 제안은 `APPLIED`, 일정 목록에는 새 일정이 표시된다.
   - 동률 또는 반대 우세: AI 제안은 `REJECTED`로 표시된다.
7. 여러 클라이언트가 동시에 종료를 시도해 `Vote is already closed` 또는 유사한 `409`가 발생하면 실패 토스트 대신 투표 상세를 다시 조회한다. 재조회 결과가 `CLOSED`이면 정상 종료로 처리한다.
8. 전원 투표 전 종료 API가 `409`를 반환하면 종료 상태로 바꾸지 말고 최신 투표 정보를 다시 조회한 뒤 안내 문구를 유지한다.
9. 일정 충돌로 종료 API가 `409`를 반환하면 투표는 계속 `OPEN` 상태이므로 완료 처리하지 말고 기존 일정 충돌 오류를 표시한다.

## 상태 처리 예시

```ts
const submitBallot = async (voteOptionId: number) => {
  const updatedVote = await putBallot(tripId, voteId, { voteOptionId });
  setVote(updatedVote);

  if (
    updatedVote.status === "OPEN" &&
    updatedVote.allMembersVoted &&
    canEditTrip
  ) {
    await closeVoteAndRefresh();
  }
};
```

`closeVoteAndRefresh`는 중복 호출 방지 플래그를 사용하고, 성공 시 투표·AI 제안·일정 관련 캐시를 모두 무효화하거나 다시 조회하도록 구현해줘. 기존 프로젝트의 API 클라이언트, 서버 상태 관리, 에러 처리, 모달 패턴을 그대로 재사용하고 새로운 상태 관리 라이브러리는 추가하지 마.
