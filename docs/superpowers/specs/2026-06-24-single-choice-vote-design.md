# Single-choice Vote Design

## Scope

Add trip-scoped, single-choice voting using the existing `VOTE`, `VOTE_OPTION`, and `VOTE_BALLOT` tables.

## Rules

- Trip owners and editors can create and close votes.
- Any accepted trip member can list votes, view vote results, and cast a ballot.
- A vote has a non-blank title and at least two options.
- Each user has one ballot per vote. Voting again replaces the previous option.
- Closed votes remain readable but reject new or changed ballots.
- Every response includes option vote counts and the current user's selected option.
- Vote editing and deletion are outside this iteration.

## API

- `POST /api/trips/{tripId}/votes`
- `GET /api/trips/{tripId}/votes`
- `GET /api/trips/{tripId}/votes/{voteId}`
- `PUT /api/trips/{tripId}/votes/{voteId}/ballot`
- `PATCH /api/trips/{tripId}/votes/{voteId}/close`

## Persistence

Keep the existing unique key `VOTE_BALLOT(vote_id, user_id)` and use an upsert to implement ballot replacement atomically. Validate that the selected option belongs to the target vote before writing.

## Errors

- `400`: invalid title, fewer than two options, invalid option
- `401`: missing authenticated user
- `403`: inaccessible trip or insufficient edit permission
- `404`: vote not found in the trip
- `409`: vote already closed

## Verification

Service tests cover permissions, creation, result aggregation, ballot replacement, invalid options, and closed votes. Controller tests cover authenticated-user forwarding. The complete Maven test suite verifies integration compatibility.
