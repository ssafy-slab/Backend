# Place Sort Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 여행지 카드에 리뷰 요약을 표시하고 선택형 리뷰 정렬을 제공한다.

**Architecture:** 프론트가 선택된 정렬 키를 장소 검색 API에 전달하고 MyBatis가 검색 관련도 다음에 안전하게 고정된 정렬 분기를 적용한다.

**Tech Stack:** Spring Boot, MyBatis, Vue 3, TypeScript, Vitest

---

### Task 1: 백엔드 정렬 계약

- [ ] `PlaceSearchRequest`에 정렬 키를 추가하고 허용값을 정규화한다.
- [ ] 컨트롤러·서비스·매퍼에 정렬 키를 전달한다.
- [ ] MyBatis에 추천·리뷰 수·평점 정렬 분기를 추가한다.
- [ ] 서비스 및 XML 테스트를 통과시킨다.

### Task 2: 프론트 정렬 UI

- [ ] 장소 API에 정렬 파라미터를 추가한다.
- [ ] 탐색 목록에 세 정렬 토글 버튼을 추가한다.
- [ ] 같은 버튼 재클릭 시 일반 순서로 복귀한다.
- [ ] 초기화 시 정렬을 해제한다.
- [ ] 카드와 선택 장소 패널에 평점·리뷰 수를 표시한다.

### Task 3: 검증

- [ ] 백엔드 전체 테스트를 실행한다.
- [ ] 프론트 전체 테스트, 타입 검사, 빌드를 실행한다.
