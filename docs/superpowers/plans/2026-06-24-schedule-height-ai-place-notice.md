# Schedule Height and AI Place Notice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand the desktop full-schedule panel and suppress the unlinked-place notice on AI suggestion cards.

**Architecture:** Keep the existing Vue component and API behavior. Change only presentation classes and conditional content, with component tests protecting both requirements.

**Tech Stack:** Vue 3, TypeScript, Tailwind CSS, Vitest

---

### Task 1: Lock the presentation requirements

**Files:**
- Modify: `C:/ssafy/final/Frontend/src/pages/schedule/ui/ScheduleDetailPage.test.ts`

- [x] Add a test asserting the schedule panel has `xl:min-h-[480px]` and `xl:max-h-[calc(100vh-300px)]`.
- [x] Add an assertion that the DB linkage notice is absent.
- [x] Run the component test and confirm the new assertions fail.

### Task 2: Apply the minimal UI changes

**Files:**
- Modify: `C:/ssafy/final/Frontend/src/pages/schedule/ui/ScheduleDetailPage.vue`

- [x] Update the desktop schedule height classes.
- [x] Remove the DB linkage warning paragraph.
- [x] Run the component test and confirm it passes.

### Task 3: Verify regressions

- [x] Run all frontend tests.
- [x] Run the frontend type-check and production build.
