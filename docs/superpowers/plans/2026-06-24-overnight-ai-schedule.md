# Overnight AI Schedule Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 자정을 넘는 AI 여행 일정을 최대 12시간, 여행 종료 다음 날 06:00 경계 안에서 검증하고 겹침까지 정확히 판정한다.

**Architecture:** 저장 모델은 변경하지 않고 `scheduleDate`를 시작일로 유지한다. 검증 시 `LocalDateTime` 구간으로 변환하여 여행 범위, 지속 시간, 기존 일정 및 생성 일정 겹침을 하나의 시간축에서 검사한다.

**Tech Stack:** Java 17, Spring Boot, JUnit 5, AssertJ

---

### Task 1: Overnight boundary validation

**Files:**
- Modify: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidatorTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidator.java`

- [ ] **Step 1: Write failing boundary tests**

Add tests that assert:

```java
// Same-day 23:59 is accepted.
item(LocalDate.of(2026, 7, 1), LocalTime.of(22, 0), LocalTime.of(23, 59), "야경")

// Final-day overnight ending at 06:00 is accepted.
item(LocalDate.of(2026, 7, 3), LocalTime.of(23, 0), LocalTime.of(6, 0), "새벽 산책")

// Final-day overnight ending at 06:01 is rejected.
item(LocalDate.of(2026, 7, 3), LocalTime.of(23, 0), LocalTime.of(6, 1), "새벽 산책")

// Exactly 12 hours is accepted; more than 12 hours is rejected.
item(LocalDate.of(2026, 7, 1), LocalTime.of(18, 0), LocalTime.of(6, 0), "야간 일정")
item(LocalDate.of(2026, 7, 1), LocalTime.of(18, 0), LocalTime.of(6, 1), "너무 긴 일정")
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\mvnw.cmd -Dtest=AiScheduleSlotValidatorTest test
```

Expected: new overnight acceptance tests fail because the current validator rejects `endTime <= startTime` or times after 23:00.

- [ ] **Step 3: Implement LocalDateTime interval conversion**

In `AiScheduleSlotValidator`:

- Keep `DAY_START = 07:00`.
- Add `FINAL_DAY_CUTOFF = 06:00` and `MAX_DURATION = 12 hours`.
- Convert each item into a start `LocalDateTime`.
- Use the same date when `endTime > startTime`; otherwise use the next date.
- Reject duration over 12 hours.
- Reject starts outside the trip range.
- Permit final-day overflow only through the next day at 06:00.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run:

```powershell
.\mvnw.cmd -Dtest=AiScheduleSlotValidatorTest test
```

Expected: all boundary tests pass.

### Task 2: Cross-date overlap validation

**Files:**
- Modify: `src/test/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidatorTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/AiScheduleSlotValidator.java`

- [ ] **Step 1: Write failing overlap tests**

Add tests where:

```java
// Existing July 1 23:00~July 2 01:00 overlaps a July 2 00:30~02:00 suggestion.
// Generated July 1 23:00~July 2 01:00 overlaps a generated July 2 00:30~02:00 item.
```

Both must throw `ResponseStatusException`.

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
.\mvnw.cmd -Dtest=AiScheduleSlotValidatorTest test
```

Expected: the existing date-equality overlap implementation misses both cross-date overlaps.

- [ ] **Step 3: Compare real date-time intervals**

Replace `LocalTime` overlap checks with `LocalDateTime` interval checks:

```java
return start.isBefore(otherEnd) && otherStart.isBefore(end);
```

Build existing schedule intervals using their own `scheduleDate`; infer one hour when their end time is absent and infer next-day end when it is not after the start.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run:

```powershell
.\mvnw.cmd -Dtest=AiScheduleSlotValidatorTest test
```

Expected: all boundary and overlap tests pass.

### Task 3: Align the AI prompt

**Files:**
- Modify: `src/test/java/com/ssafy/ssafy_slap/ai/service/GmsAiSchedulePromptTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/ai/service/GmsAiScheduleClient.java`

- [ ] **Step 1: Write a failing prompt test**

Reflectively read `SYSTEM_PROMPT` and assert that it contains:

```java
"scheduleDate is the start date"
"06:00"
"12 hours"
```

- [ ] **Step 2: Run prompt test and verify RED**

Run:

```powershell
.\mvnw.cmd -Dtest=GmsAiSchedulePromptTest test
```

Expected: assertions fail because the prompt only describes 07:00–23:00 availability.

- [ ] **Step 3: Update the system prompt**

State that same-day schedules may end through 23:59, `endTime <= startTime` means next-day completion, duration is at most 12 hours, and a final-day schedule may end by 06:00 the next day.

- [ ] **Step 4: Run prompt test and verify GREEN**

Run:

```powershell
.\mvnw.cmd -Dtest=GmsAiSchedulePromptTest test
```

Expected: prompt test passes.

### Task 4: Full verification

**Files:**
- Verify all modified production and test files.

- [ ] **Step 1: Run focused AI tests**

```powershell
.\mvnw.cmd -Dtest=AiScheduleSlotValidatorTest,GmsAiSchedulePromptTest test
```

- [ ] **Step 2: Run the full backend test suite**

```powershell
.\mvnw.cmd test
```

- [ ] **Step 3: Inspect the final diff**

```powershell
git diff --check
git diff -- src/main/java/com/ssafy/ssafy_slap/ai src/test/java/com/ssafy/ssafy_slap/ai docs/superpowers
```

Expected: no whitespace errors, no unrelated changes, and all specified boundary cases are covered.

