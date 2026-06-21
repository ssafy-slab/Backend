# My Reviews Tab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 마이페이지에서 현재 사용자의 장소 리뷰 목록을 조회하고 상세 이동·삭제할 수 있게 한다.

**Architecture:** 리뷰 모듈에 사용자별 리뷰 조회 쿼리와 인증 API를 추가한다. 프론트 리뷰 API가 목록을 가져오고 ProfilePage가 탭 상태와 삭제를 관리하며, App이 장소 단건 조회 후 상세 화면을 연다.

**Tech Stack:** Spring Boot, Spring Security JWT, MyBatis, Vue 3, TypeScript, Vitest

---

### Task 1: 사용자별 리뷰 조회 API

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/review/dto/MyPlaceReviewResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/review/controller/MyReviewController.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/review/mapper/ReviewMapper.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/review/service/ReviewService.java`
- Modify: `src/main/resources/mapper/review/ReviewMapper.xml`
- Test: `src/test/java/com/ssafy/ssafy_slap/review/service/ReviewServiceTest.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/review/controller/MyReviewControllerTest.java`

- [ ] 실패하는 서비스·컨트롤러 테스트를 작성한다.
- [ ] 사용자별 최신순 리뷰 조회를 구현한다.
- [ ] 인증되지 않은 요청을 거부한다.

### Task 2: 프론트 내 리뷰 탭

**Files:**
- Modify: `src/entities/review/api/reviewApi.ts`
- Modify: `src/entities/place/api/placeApi.ts`
- Modify: `src/pages/profile/ui/ProfilePage.vue`
- Modify: `src/app/App.vue`
- Test: `src/entities/review/api/reviewApi.test.ts`
- Test: `src/pages/profile/ui/ProfilePage.test.ts`

- [ ] 내 리뷰 API 테스트를 작성한다.
- [ ] 마이페이지 탭, 카드, 빈 상태, 삭제를 구현한다.
- [ ] 여행지 단건 조회 후 상세 화면으로 이동한다.
- [ ] 프론트 테스트, 타입 검사, 빌드를 통과시킨다.

### Task 3: 최종 검증

- [ ] 백엔드 전체 테스트를 실행한다.
- [ ] 프론트 전체 테스트, 타입 검사, 빌드를 실행한다.
- [ ] 두 저장소의 diff를 확인한다.
