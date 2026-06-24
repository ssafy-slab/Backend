# Community Multi-Image Upload Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Save community posts with more than five text/image cells and enforce a 5MB image limit consistently in the frontend and backend.

**Architecture:** Keep the existing per-image upload flow and ordered cell payload. Remove the backend application-level cell-count rejection, then align frontend validation and guidance with the backend's 5MB multipart and storage limits.

**Tech Stack:** Java 21, Spring Boot, JUnit 5, Mockito, Vue 3, TypeScript, Vitest

---

### Task 1: Accept posts containing more than five cells

**Files:**
- Modify: `src/test/java/com/ssafy/ssafy_slap/community/service/CommunityServiceTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/community/service/CommunityService.java`

- [ ] **Step 1: Replace the rejection test with a persistence regression test**

Change the existing `rejectsPostWithMoreThanFiveCells` test into a test that creates six cells, stubs post persistence and retrieval, invokes `createPost`, and verifies all six cells are passed to `insertPostCells`.

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```powershell
mvn -Dtest=CommunityServiceTest#createsPostWithMoreThanFiveCells test
```

Expected: FAIL because `CommunityService` throws `400 BAD_REQUEST`.

- [ ] **Step 3: Remove the five-cell validation**

Delete `MAX_POST_CELLS` and the `request.cells().size() > MAX_POST_CELLS` branch. Preserve cell normalization, ordering, and per-cell validation.

- [ ] **Step 4: Run the focused backend tests**

Run:

```powershell
mvn -Dtest=CommunityServiceTest test
```

Expected: PASS.

### Task 2: Align frontend image validation to 5MB

**Files:**
- Modify: `../slap-frontend/src/pages/community/ui/CommunityEditorPage.test.ts`
- Modify: `../slap-frontend/src/pages/community/ui/CommunityEditorPage.vue`

- [ ] **Step 1: Add a frontend regression test**

Add tests proving that a 3MB `image/jpeg` reaches the upload API and that a file larger than 5MB displays `이미지는 5MB 이하만 업로드할 수 있습니다.` without uploading.

- [ ] **Step 2: Run the focused frontend test and verify it fails**

Run:

```powershell
corepack pnpm exec vitest run src/pages/community/ui/CommunityEditorPage.test.ts
```

Expected: FAIL while the frontend still enforces the old 2MB limit.

- [ ] **Step 3: Change the frontend limit and helper text**

Change the file-size comparison, validation error, and visible upload guidance to 5MB.

- [ ] **Step 4: Run the focused frontend tests**

Run:

```powershell
corepack pnpm exec vitest run src/pages/community/ui/CommunityEditorPage.test.ts
```

Expected: PASS.

### Task 3: Update documentation and verify

**Files:**
- Verify: `src/main/resources/application.properties`
- Verify: `src/main/java/com/ssafy/ssafy_slap/community/service/CommunityImageStorageService.java`
- Verify: `docs/community-s3-images.md`

- [ ] **Step 1: Confirm all backend image limits are 5MB**

Verify Spring multipart configuration, service validation, and documentation all state 5MB.

- [ ] **Step 2: Run backend community tests**

Run:

```powershell
mvn -Dtest=CommunityServiceTest,CommunityImageStorageServiceTest test
```

Expected: PASS.

- [ ] **Step 3: Run frontend type checking**

Run:

```powershell
corepack pnpm type-check
```

Expected: PASS.

- [ ] **Step 4: Review the final diff**

Confirm the diff contains only the cell-limit removal, 5MB image-limit alignment, regression tests, and supporting design/plan documents.
