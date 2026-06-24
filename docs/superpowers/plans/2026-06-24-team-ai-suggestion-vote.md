# Team AI Suggestion Vote Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route team-trip AI suggestions through an explicit approve/reject vote while preserving direct apply/reject for personal trips.

**Architecture:** `AiSuggestionVoteService` creates and links a general vote to one AI suggestion. `AiSuggestionVoteOutcomeProcessor` participates in vote closing and resolves the linked suggestion transactionally, creating a schedule only when approval wins.

**Tech Stack:** Java 17, Spring Boot, MyBatis, MySQL, JUnit 5, Mockito

---

### Task 1: Define team routing behavior

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiSuggestionVoteServiceTest.java`
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiSuggestionVoteOutcomeProcessorTest.java`

- [x] Write a failing test that creates one approve/reject vote for a pending team suggestion.
- [x] Write failing tests for non-team rejection and duplicate vote rejection.
- [x] Write failing tests for approval, rejection, and schedule-conflict rollback behavior.
- [x] Run isolated tests and confirm missing classes/methods fail.

### Task 2: Add persistence and services

**Files:**
- Modify: `src/main/resources/schema/schema.sql`
- Create: `docs/sql/2026-06-24-ai-suggestion-votes.sql`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/domain/AiSuggestionVote.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiSuggestionVoteService.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiSuggestionVoteOutcomeProcessor.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/mapper/AiSuggestionMapper.java`
- Modify: `src/main/resources/mapper/ai/AiSuggestionMapper.xml`

- [x] Add the one-to-one link table and migration.
- [x] Add team-type, link lookup, status transition, and resolution mapper operations.
- [x] Create votes with `찬성` and `반대` options and mark suggestions `VOTING`.
- [x] Resolve linked suggestions during vote close.

### Task 3: Connect APIs and enforce trip type

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiSuggestionService.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/controller/AiSuggestionController.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiSuggestionResponse.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/vote/service/VoteService.java`

- [x] Add the suggestion vote endpoint.
- [x] Block direct apply and bulk apply on team trips.
- [x] Include linked `voteId` in suggestion responses.
- [x] Invoke the AI outcome processor before closing a linked vote.

### Task 4: Verify and document

**Files:**
- Modify: `docs/trip-api.md`
- Modify: `docs/vote-api.md`
- Create: `docs/frontend-team-ai-suggestion-vote-prompt.md`

- [x] Run isolated AI-vote and vote tests.
- [x] Parse changed MyBatis XML.
- [x] Run `git diff --check`.
- [x] Record unrelated repository compilation failures without changing those files.
