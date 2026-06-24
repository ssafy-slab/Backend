# AI Existing Schedule Context Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate conflict-free AI schedule suggestions using existing trip schedules as context.

**Architecture:** Extend the AI client contract with existing schedule items, serialize them into the GMS prompt, and validate/normalize returned slots in a reusable component before suggestions are persisted or returned.

**Tech Stack:** Java 17, Spring Boot, MyBatis, JUnit 5, Mockito

---

### Task 1: Define prompt and validation behavior

**Files:**
- Modify: `src/test/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClientTest.java`
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidatorTest.java`

- [x] Test that existing schedule dates/times/titles and the `07:00~23:00`, one-hour rules are included in the AI request.
- [x] Test one-hour end-time normalization.
- [x] Test rejection of existing-schedule overlap, generated-suggestion overlap, and out-of-hours output.
- [x] Run targeted tests and confirm failure.

### Task 2: Extend the AI input contract

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleClient.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClient.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisService.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleDraftService.java`

- [x] Load existing schedules for both durable analysis and draft generation.
- [x] Pass schedules to the AI client.
- [x] Add prompt instructions and existing schedule context.

### Task 3: Validate and normalize AI slots

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidator.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisService.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleDraftService.java`

- [x] Normalize missing `endTime` to `startTime + 1 hour`.
- [x] Reject slots outside `07:00~23:00`.
- [x] Reject overlap with existing schedules and sibling suggestions.
- [x] Preserve `NO_RESULT` from the AI when no slot exists.

### Task 4: Verify and document

**Files:**
- Modify: `docs/trip-api.md`

- [x] Run targeted tests.
- [x] Run the full Maven test suite.
- [x] Run `git diff --check`.
