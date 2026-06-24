# Refresh Token Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 모든 일반/OAuth 로그인에 회전형 Refresh Token 세션을 적용한다.

**Architecture:** Access Token은 짧은 JWT로 유지하고 Refresh Token은 난수 원문을 HttpOnly 쿠키에, SHA-256 해시를 MySQL에 저장한다. 재발급 시 행 잠금과 트랜잭션으로 기존 토큰을 폐기한 후 새 토큰을 발급한다.

**Tech Stack:** Java 17, Spring Boot, Spring Security, MyBatis, MySQL, JUnit 5, Mockito

---

### Task 1: Refresh Token 저장소

- [x] `REFRESH_TOKEN` 스키마와 배포 SQL을 추가한다.
- [x] Refresh Token 도메인, MyBatis mapper와 XML을 추가한다.
- [x] 해시 저장, 만료, 폐기 필드를 정의한다.

### Task 2: 발급·회전·폐기 서비스

- [x] 난수 Refresh Token 발급 테스트를 먼저 작성하고 실패를 확인한다.
- [x] 토큰 회전과 재사용 감지 테스트를 작성한다.
- [x] `RefreshTokenService`와 `AuthSessionService`를 구현한다.

### Task 3: 일반 로그인 API

- [x] 로그인·회원가입 응답에서 Refresh Token이 JSON으로 노출되지 않는 테스트를 작성한다.
- [x] `/api/auth/refresh`, `/api/auth/logout`과 CSRF 방어 헤더를 구현한다.
- [x] HttpOnly/Secure/SameSite 쿠키 생성과 삭제를 구현한다.

### Task 4: OAuth와 계정 보안 이벤트

- [x] OAuth 티켓 교환 응답에서 Refresh 쿠키를 발급한다.
- [x] 비밀번호 변경·재설정·회원 탈퇴 시 모든 세션을 폐기한다.

### Task 5: 검증과 프론트 인계

- [x] 인증 관련 표적 테스트를 실행한다.
- [x] 전체 Maven 테스트를 실행한다.
- [x] 프론트 구현 프롬프트와 API 계약을 작성한다.
