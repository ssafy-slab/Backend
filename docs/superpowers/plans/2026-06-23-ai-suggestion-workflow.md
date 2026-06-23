# AI Suggestion Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist AI-generated schedule suggestions, expose durable listing, and support individual or bulk application and rejection.

**Architecture:** `AiSuggestionMapper` owns the three AI tables. `AiSuggestionService` persists generated drafts and applies suggestions to `SCHEDULE_ITEM` transactionally. The existing button endpoint becomes a persisted analysis endpoint; automatic 30-message triggering is initiated after chat commit and publishes a WebSocket completion event.

**Tech Stack:** Java 17, Spring Boot 4, MyBatis, MySQL, WebSocket, JUnit 5, Mockito

---

### Task 1: Persistence model

- [x] Add failing mapper/service tests for stored and listed suggestions.
- [x] Add AI run, state, and suggestion domain/DTO/mapper files.
- [x] Add schema and migration SQL matching the deployed tables.
- [x] Verify targeted tests pass.

### Task 2: Apply and reject workflow

- [x] Add failing tests for individual and bulk apply/reject.
- [x] Insert `SCHEDULE_ITEM` and update `AI_SUGGESTION` in one transaction.
- [x] Reject non-pending suggestions and enforce trip edit access.
- [x] Verify targeted tests pass.

### Task 3: Trigger and delivery

- [x] Add failing tests for button persistence and 30-message automatic triggering.
- [x] Persist button and automatic analysis runs.
- [x] Broadcast `AI_ANALYSIS_COMPLETED` after successful storage.
- [x] Expose list, item apply/reject, and bulk apply/reject endpoints.
- [x] Verify controller and trigger tests pass.

### Task 4: Integration verification

- [x] Run the complete test suite.
- [x] Build the deployable package.
- [x] Confirm secrets are absent from changed files.
