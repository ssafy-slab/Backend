# Liked Content Unification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace community bookmarks with community likes and add persistent place likes visible from My Page.

**Architecture:** Use idempotent POST/DELETE endpoints for both content types. Store place likes in a new join table, migrate bookmark rows into the existing community-like table, and expose separate liked-post and liked-place queries behind one frontend tab.

**Tech Stack:** Java, Spring Boot, MyBatis, MySQL, Vue 3, TypeScript, Vitest

---

### Task 1: Community like migration and API

- [ ] Replace bookmark service/mapper/controller tests with liked-post tests.
- [ ] Verify the tests fail against bookmark-based behavior.
- [ ] Add migration SQL, remove bookmark schema and DTO fields, and implement idempotent add/remove/list likes.
- [ ] Run community and user controller tests.

### Task 2: Place like persistence and API

- [ ] Add failing service/controller/mapper tests for place likes and liked-place listing.
- [ ] Add `PLACE_LIKE`, authenticated place endpoints, mapper operations, and user listing endpoint.
- [ ] Run focused place and user tests.

### Task 3: Frontend API contracts

- [ ] Replace bookmark API tests with explicit community-like and liked-post tests.
- [ ] Add failing place-like and liked-place API tests.
- [ ] Implement authenticated API functions and response mapping.
- [ ] Run API tests.

### Task 4: Community and place UI

- [ ] Update page tests to expect heart controls and persistent likes.
- [ ] Remove bookmark UI and connect community and place heart controls.
- [ ] Run focused page tests.

### Task 5: My Page liked-content tabs

- [ ] Replace bookmark tests with liked post/place sub-tab tests.
- [ ] Implement the unified "좋아요" tab with post/place sub-tabs, removal, and navigation.
- [ ] Run ProfilePage tests.

### Task 6: Verification

- [ ] Run all focused backend tests.
- [ ] Run all affected frontend tests.
- [ ] Run frontend type checking.
- [ ] Search for obsolete bookmark symbols and review the final diff.
