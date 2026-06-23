# AI Place Matching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve AI-provided place names against the local `PLACE` table before storing schedule suggestions.

**Architecture:** GMS returns `placeName` and `regionHint`. A focused matcher queries exact normalized place-name candidates and accepts only a unique region-compatible result. AI suggestion persistence stores both source text fields and the verified place ID.

**Tech Stack:** Java 17, Spring Boot, MyBatis, MySQL, JUnit 5, Mockito

---

### Task 1: AI place fields

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiScheduleDraftItem.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClient.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClientTest.java`

- [x] Write a failing parsing test for `placeName` and `regionHint`.
- [x] Replace `placeId` in the AI contract with the two text fields.
- [x] Run the GMS client test and confirm it passes.

### Task 2: Conservative database matching

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiPlaceMatcher.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiPlaceCandidate.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/place/mapper/PlaceMapper.java`
- Modify: `src/main/resources/mapper/place/PlaceMapper.xml`
- Test: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiPlaceMatcherTest.java`

- [x] Write failing tests for unique, ambiguous, and missing matches.
- [x] Add exact normalized-name candidate lookup.
- [x] Accept only one candidate compatible with the optional region hint.
- [x] Run matcher tests and confirm they pass.

### Task 3: Suggestion persistence and response

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/domain/AiSuggestion.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiSuggestionResponse.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisService.java`
- Modify: `src/main/resources/mapper/ai/AiAnalysisMapper.xml`
- Modify: `src/main/resources/mapper/ai/AiSuggestionMapper.xml`
- Test: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiAnalysisServiceTest.java`

- [x] Write a failing test proving verified ID and source place text are stored.
- [x] Match each AI item before constructing `AiSuggestion`.
- [x] Persist and return both source text fields.
- [x] Run AI service and controller tests.

### Task 4: Schema and verification

**Files:**
- Modify: `src/main/resources/schema/schema.sql`
- Modify: `docs/sql/2026-06-23-ai-analysis-suggestions.sql`
- Modify: `docs/trip-api.md`

- [x] Add `suggested_place_name` and `suggested_region_hint` to canonical SQL.
- [x] Document nullable matching behavior and response fields.
- [x] Run all tests and package the application.
