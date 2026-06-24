# AI Vote Confirmation Modal Design

## Goal

Replace the browser confirmation dialog used to create a team vote with an in-app modal, and align the rejected status with other card actions.

## Behavior

- Clicking `투표 올리기` opens a modal without calling the API.
- The modal shows the suggestion title, date, time, and place.
- `취소` closes the modal without creating a vote.
- Modal `투표 올리기` creates the vote.
- While creating, the confirmation button is disabled.
- Success closes the modal and switches to voting suggestions.
- Failure keeps the modal open so the user can retry.
- The `거절됨` status uses the same height and vertical centering as `일정 반영 완료`.
