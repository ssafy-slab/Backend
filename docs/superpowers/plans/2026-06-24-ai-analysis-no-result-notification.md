# AI Analysis No-Result Notification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Notify trip clients through WebSocket when AI analysis completes without enough schedule-related context to create suggestions.

**Architecture:** Extend the AI JSON contract with `resultStatus`, `reasonCode`, and `message` while preserving all existing JSON field names. `AiAnalysisService` treats `NO_RESULT` as a completed, non-retryable analysis, advances the analyzed-message cursor, and broadcasts `AI_ANALYSIS_NO_RESULT`; infrastructure and malformed-response failures keep the existing failed/retry behavior.

**Tech Stack:** Java 17, Spring Boot 4, Jackson, MyBatis, WebSocket, JUnit 5, Mockito

---

### Task 1: Define the AI no-result contract

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiScheduleDraftResponse.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClient.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClientTest.java`

- [ ] Add a failing test that parses `resultStatus: NO_RESULT`, `reasonCode`, `message`, and an empty `schedules` array.
- [ ] Run `GmsAiScheduleClientTest` and confirm the new assertions fail.
- [ ] Add the three response fields while preserving the existing three-argument constructor used by current callers.
- [ ] Update the system prompt to require `SUCCESS` with schedules or `NO_RESULT` with a Korean user message.
- [ ] Re-run `GmsAiScheduleClientTest` and confirm it passes.

### Task 2: Broadcast no-result analyses

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisNotifier.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/WebSocketAiAnalysisNotifier.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisService.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisServiceTest.java`
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/WebSocketAiAnalysisNotifierTest.java`

- [ ] Add a failing service test asserting that `NO_RESULT` stores no suggestion, completes state, and calls `notifier.noResult(...)`.
- [ ] Add a failing notifier test asserting the exact `AI_ANALYSIS_NO_RESULT` JSON payload.
- [ ] Run the focused tests and confirm they fail for missing behavior.
- [ ] Add `noResult` to the notifier interface and WebSocket implementation.
- [ ] Branch in `AiAnalysisService` before suggestion persistence, mark the run successful, complete state, broadcast the reason, and return an empty successful response.
- [ ] Re-run focused tests and confirm they pass.

### Task 3: Document and verify

**Files:**
- Modify: `docs/trip-api.md`

- [ ] Document `AI_ANALYSIS_NO_RESULT`, its fields, and the distinction from system failure.
- [ ] Run `mvn test` with JDK 21.
- [ ] Confirm all tests pass and `git diff --check` reports no whitespace errors.
