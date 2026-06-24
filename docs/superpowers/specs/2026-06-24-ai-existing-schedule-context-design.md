# AI Existing Schedule Context Design

## Goal

Allow AI analysis to place schedule suggestions even when chat messages omit a date or time by giving the model the trip's existing schedules and explicit availability rules.

## Rules

- Send all existing `SCHEDULE_ITEM` rows for the trip with the trip and chat messages.
- Suggestions must stay within the trip date range.
- Available hours are `07:00` inclusive through `23:00` exclusive.
- When duration is not stated, use one hour.
- Suggestions must not overlap existing schedules or other suggestions in the same AI response.
- Prefer contextually appropriate placement; when context gives no preference, choose the earliest available slot.
- If no free one-hour slot exists in the trip period, return `NO_RESULT`.
- Backend validation rejects out-of-hours and overlapping AI output before persistence.

## Architecture

`AiAnalysisService` and the draft service load schedules through `TripScheduleMapper` and pass them to `AiScheduleClient`. `GmsAiScheduleClient` serializes existing schedules and the placement rules into the prompt. A focused validator normalizes a missing end time to one hour and validates bounds and overlap.

## Compatibility

The API response shape remains unchanged. Existing schedules are read-only AI context and are never modified by analysis.
