# Schedule Detail Layout and Pagination Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize the schedule detail screen into a balanced two-column workspace and paginate AI suggestions three at a time.

**Architecture:** Keep the existing page component and API behavior. Add local computed pagination state, render paged suggestion groups, and move the checklist into the left rail below the schedule.

**Tech Stack:** Vue 3, TypeScript, Tailwind CSS, Vitest

---

### Task 1: Pagination behavior

- [x] Add a component test covering three cards per page and previous/next navigation.
- [x] Run the test and confirm it fails.
- [x] Add page state, slicing, grouping, reset, and clamp behavior.
- [x] Run the test and confirm it passes.

### Task 2: Layout

- [x] Replace the desktop three-column grid with a two-column grid.
- [x] Move the checklist below the full schedule in the left rail.
- [x] Keep chat and AI suggestions in the right workspace.
- [x] Preserve single-column responsive behavior.

### Task 3: Verification

- [x] Run frontend tests.
- [x] Run type-check and production build.
- [ ] Inspect the page at desktop and mobile widths.
