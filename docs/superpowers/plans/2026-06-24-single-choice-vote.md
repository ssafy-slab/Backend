# Single-choice Vote Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add trip-scoped single-choice vote creation, lookup, ballot replacement, and closing.

**Architecture:** A feature-local controller delegates authorization and transaction rules to `VoteService`. `VoteMapper` and MyBatis XML persist the existing three-table model; responses are assembled from a vote, its counted options, and the current user's ballot.

**Tech Stack:** Java 17, Spring Boot, Spring MVC, MyBatis, MySQL, JUnit 5, Mockito, AssertJ

---

### Task 1: Define vote behavior with failing service tests

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/vote/service/VoteServiceTest.java`

- [x] Test editable-trip creation with two normalized options.
- [x] Test rejection of fewer than two options.
- [x] Test accessible-trip result lookup with counts and current selection.
- [x] Test ballot upsert, invalid option rejection, and closed-vote conflict.
- [x] Run the test and confirm the feature is missing. The repository Maven wrapper failed first, and direct Maven then exposed unrelated pre-existing compilation errors.

### Task 2: Implement vote domain, DTO, mapper, and service

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/domain/Vote.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/domain/VoteOption.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/dto/VoteCreateRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/dto/VoteOptionRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/dto/VoteBallotRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/dto/VoteOptionResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/dto/VoteResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/mapper/VoteMapper.java`
- Create: `src/main/resources/mapper/vote/VoteMapper.xml`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/service/VoteService.java`

- [x] Implement trip access and edit checks matching checklist authorization.
- [x] Insert vote and options in one transaction.
- [x] Read options with grouped ballot counts.
- [x] Upsert one ballot per `(vote_id, user_id)`.
- [x] Close only an open vote.
- [x] Run the isolated vote service tests and confirm they pass.

### Task 3: Add REST endpoints

**Files:**
- Create: `src/test/java/com/ssafy/ssafy_slap/vote/controller/VoteControllerTest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/vote/controller/VoteController.java`

- [x] Write controller tests for authenticated user forwarding and anonymous rejection.
- [x] Run the controller test after the RED service stage.
- [x] Implement the five trip-scoped endpoints.
- [x] Run vote controller and service tests.

### Task 4: Verify

- [x] Attempt the full test suite; record that unrelated AI, chat, and weather sources currently fail compilation.
- [x] Inspect `git diff --check`.
- [x] Confirm unrelated pre-existing AI changes remain untouched.
