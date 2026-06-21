# Place Review Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 여행지별 별점 리뷰 CRUD와 평균 별점·리뷰 수를 백엔드와 프론트엔드에 연결한다.

**Architecture:** `review` 모듈이 리뷰 저장, 조회, 집계를 담당하고 장소 조회 쿼리는 리뷰 집계 결과를 함께 반환한다. 프론트는 별도의 리뷰 API 모듈을 통해 상세 화면의 별점 입력, 내 리뷰 편집, 리뷰 목록과 요약 갱신을 관리한다.

**Tech Stack:** Spring Boot, Spring Security JWT, MyBatis, MySQL, JUnit 5, Mockito, Vue 3, TypeScript, Pinia, Vitest, Tailwind CSS

---

### Task 1: 백엔드 리뷰 서비스 계약

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/review/dto/PlaceReviewRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/review/dto/PlaceReviewResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/review/dto/PlaceReviewSummaryResponse.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/review/mapper/ReviewMapper.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/review/service/ReviewService.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/review/service/ReviewServiceTest.java`

- [ ] 서비스 테스트에서 등록, 중복 등록, 수정, 삭제, 검증, 비로그인 처리를 먼저 정의한다.
- [ ] 테스트가 구현 부재로 실패하는지 확인한다.
- [ ] 최소 DTO, 매퍼 인터페이스, 서비스를 구현한다.
- [ ] 서비스 테스트를 통과시킨다.

### Task 2: 백엔드 리뷰 API와 SQL

**Files:**
- Create: `src/main/java/com/ssafy/ssafy_slap/review/controller/ReviewController.java`
- Create: `src/main/resources/mapper/review/ReviewMapper.xml`
- Modify: `src/main/java/com/ssafy/ssafy_slap/global/config/SecurityConfig.java`
- Test: `src/test/java/com/ssafy/ssafy_slap/review/controller/ReviewControllerTest.java`

- [ ] 공개 조회와 인증 필수 변경 API의 컨트롤러 테스트를 작성한다.
- [ ] 테스트 실패를 확인한다.
- [ ] 컨트롤러, 인증 사용자 추출, MyBatis CRUD·집계 SQL을 구현한다.
- [ ] 리뷰 API 테스트를 통과시킨다.

### Task 3: 장소 응답에 리뷰 요약 연결

**Files:**
- Modify: `src/main/java/com/ssafy/ssafy_slap/place/dto/PlaceSummaryResponse.java`
- Modify: `src/main/resources/mapper/place/PlaceMapper.xml`
- Modify: `src/test/java/com/ssafy/ssafy_slap/place/service/PlaceServiceTest.java`

- [ ] 장소 응답이 평균 별점과 리뷰 수를 포함하는 테스트를 추가한다.
- [ ] 테스트 실패를 확인한다.
- [ ] 활성 리뷰 집계 `LEFT JOIN`과 응답 필드를 구현한다.
- [ ] 장소 테스트와 백엔드 전체 테스트를 통과시킨다.

### Task 4: 프론트 리뷰 API와 상태

**Files:**
- Create: `src/entities/review/api/reviewApi.ts`
- Create: `src/entities/review/api/reviewApi.test.ts`
- Modify: `src/entities/place/api/placeApi.ts`
- Modify: `src/entities/travel/model/travel.ts`

- [ ] 조회 및 인증 CRUD 요청 테스트를 작성한다.
- [ ] 테스트 실패를 확인한다.
- [ ] 리뷰 API 타입과 요청 함수를 구현한다.
- [ ] 장소 API의 평균 별점과 리뷰 수 변환을 실제 응답값으로 연결한다.
- [ ] API 테스트를 통과시킨다.

### Task 5: 여행지 상세 별점 리뷰 UI

**Files:**
- Modify: `src/pages/place/ui/PlaceDetailPage.vue`
- Modify: `src/app/App.vue`
- Test: `src/pages/place/ui/PlaceDetailPage.test.ts`

- [ ] 별점 필수, 내용 선택, 비로그인 안내, 수정·삭제 UI 테스트를 작성한다.
- [ ] 테스트 실패를 확인한다.
- [ ] 클릭 가능한 별 5개와 서버 리뷰 목록을 구현한다.
- [ ] 등록·수정·삭제 후 목록과 요약을 갱신한다.
- [ ] 프론트 테스트, 타입 검사, 프로덕션 빌드를 통과시킨다.

### Task 6: 최종 검증

**Files:**
- Verify all modified files

- [ ] 백엔드 전체 테스트를 실행한다.
- [ ] 프론트 전체 테스트, 타입 검사, 빌드를 실행한다.
- [ ] `git diff --check`와 변경 범위를 확인한다.
