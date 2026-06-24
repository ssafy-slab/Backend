# Vote API

All endpoints require a JWT-authenticated user who belongs to the target trip.

## Create

`POST /api/trips/{tripId}/votes`

```json
{
  "title": "저녁 메뉴",
  "options": [
    {
      "placeId": 101,
      "optionTitle": "흑돼지",
      "description": "첫날 저녁"
    },
    {
      "placeId": null,
      "optionTitle": "해산물",
      "description": null
    }
  ]
}
```

Only the trip owner or an editor can create a vote. At least two options are required.

## List and detail

- `GET /api/trips/{tripId}/votes`
- `GET /api/trips/{tripId}/votes/{voteId}`

Each vote response contains `options[].voteCount`, `totalBallotCount`, and the requesting user's `selectedOptionId`.
It also contains:

- `eligibleVoterCount`: accepted trip-member count
- `votedMemberCount`: accepted members who currently have a ballot
- `allMembersVoted`: `true` only when every accepted member has voted

## Cast or change a ballot

`PUT /api/trips/{tripId}/votes/{voteId}/ballot`

```json
{
  "voteOptionId": 501
}
```

Submitting another option replaces the user's previous choice. Closed votes return `409 Conflict`.

## Close

`PATCH /api/trips/{tripId}/votes/{voteId}/close`

Only the trip owner or an editor can close a vote.

For a vote linked to a team AI suggestion, closing also resolves the suggestion:

- Every accepted trip member must vote first. Otherwise the close request returns `409 Conflict`.
- Approval count greater than rejection count creates the schedule and marks the suggestion `APPLIED`.
- A tie or rejection majority marks the suggestion `REJECTED`.
- A schedule time conflict returns `409 Conflict` and leaves the vote open.
