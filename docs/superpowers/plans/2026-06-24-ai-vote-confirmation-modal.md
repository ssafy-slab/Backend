# AI Vote Confirmation Modal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an in-app confirmation modal for team AI suggestion votes and align the rejected status action.

**Architecture:** Store the selected suggestion as local modal state. Separate opening the modal from submitting the existing vote API request, preserving existing success and failure behavior.

**Tech Stack:** Vue 3, TypeScript, Tailwind CSS, Vitest

---

### Task 1: Add failing component tests

- [x] Verify clicking the card action opens the modal without calling the API.
- [x] Verify cancellation closes the modal.
- [x] Verify modal confirmation creates the vote and closes on success.
- [x] Verify the rejected status has centered action sizing.
- [x] Run the tests and confirm they fail.

### Task 2: Implement the modal and status alignment

- [x] Add selected-suggestion modal state.
- [x] Split modal opening from vote submission.
- [x] Render suggestion information and cancel/confirm actions.
- [x] Keep the modal open on failure and close it on success.
- [x] Apply centered action sizing to `거절됨`.
- [x] Run the component tests and confirm they pass.

### Task 3: Verify

- [x] Run all frontend tests.
- [x] Run type-check and production build.
