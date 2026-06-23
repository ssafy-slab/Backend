# AI Schedule Draft Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an authenticated API that converts recent trip chat messages into an editable, non-persisted schedule draft through GMS.

**Architecture:** Keep orchestration in `AiScheduleDraftService` and isolate the external OpenAI-compatible request in `GmsAiScheduleClient`. Reuse existing trip access and chat lookup services, return typed DTOs, and reject malformed model output before it reaches the frontend.

**Tech Stack:** Java 17, Spring Boot 4 MVC, Jackson, Java `HttpClient`, JUnit 5, Mockito

---

### Task 1: Define and test draft orchestration

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiScheduleDraftServiceTest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiScheduleDraftRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiScheduleDraftItem.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/dto/AiScheduleDraftResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleClient.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleDraftService.java`

- [x] Write tests for successful generation, empty chat rejection, and invalid generated dates.
- [x] Run the targeted `AiScheduleDraftServiceTest` and confirm the tests fail because the feature is absent.
- [x] Implement the minimum DTO, client interface, orchestration, and validation code.
- [x] Re-run the targeted test and confirm it passes.

### Task 2: Define and test the GMS HTTP adapter

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClientTest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClient.java`
- Modify: `src/main/resources/application.properties`

- [x] Write tests that verify the bearer credential, model request, OpenAI-compatible response extraction, and fenced JSON cleanup.
- [x] Run the targeted `GmsAiScheduleClientTest` and confirm failure.
- [x] Implement the HTTP adapter and environment-backed configuration.
- [x] Re-run the targeted test and confirm it passes.

### Task 3: Expose and test the REST endpoint

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/ai/controller/AiScheduleDraftControllerTest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/ai/controller/AiScheduleDraftController.java`
- Modify: `docs/trip-api.md`

- [x] Write tests for authenticated user forwarding and anonymous rejection.
- [x] Run the targeted `AiScheduleDraftControllerTest` and confirm failure.
- [x] Implement the controller and document the endpoint.
- [x] Re-run the controller test and confirm it passes.

### Task 4: Verify the integrated change

**Files:**
- Review all files above.

- [x] Run the full Maven test suite with Java 17.
- [x] Run the Maven package build with tests skipped.
- [x] Confirm no credential value appears in tracked or changed files.
