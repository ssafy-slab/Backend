# Team AI Suggestion Vote Design

## Behavior

- AI analysis continues to create `PENDING` suggestions for both personal and team trips.
- Personal trips keep the existing `apply` and `reject` actions.
- Team trips expose `create vote` and `reject`; direct apply is rejected by the backend.
- Creating a vote creates one `OPEN` vote with `찬성` and `반대` options and changes the suggestion to `VOTING`.
- Each AI suggestion can create at most one vote.
- When the vote closes, `찬성 > 반대` creates the schedule and changes the suggestion to `APPLIED`.
- A tie or `반대 >= 찬성` changes the suggestion to `REJECTED`.
- If approved schedule creation conflicts with an existing schedule, the close transaction rolls back and the vote remains open.

## API

- Existing personal action: `POST /api/trips/{tripId}/ai/suggestions/{suggestionId}/apply`
- Existing shared rejection: `PATCH /api/trips/{tripId}/ai/suggestions/{suggestionId}/reject`
- New team action: `POST /api/trips/{tripId}/ai/suggestions/{suggestionId}/vote`
- Existing vote close: `PATCH /api/trips/{tripId}/votes/{voteId}/close`

Suggestion responses include `voteId` when a vote exists.

## Persistence

Add `AI_SUGGESTION_VOTE` with unique foreign keys to `AI_SUGGESTION` and `VOTE`, plus explicit approve/reject option IDs and resolution metadata. This keeps general votes independent from AI behavior and prevents duplicate vote creation.

## Permissions

Creating a suggestion vote, rejecting a suggestion, applying a personal suggestion, and closing a vote require trip edit access. Casting a ballot requires accepted trip membership.

## Frontend contract

The frontend uses `tripType` and suggestion `status`:

- Personal + `PENDING`: show apply/reject.
- Team + `PENDING`: show create-vote/reject.
- Team + `VOTING`: link to `voteId`; disable suggestion actions.
- `APPLIED` or `REJECTED`: show terminal state.
