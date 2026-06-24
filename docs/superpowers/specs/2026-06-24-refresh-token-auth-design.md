# Refresh Token 인증 설계

## 목표

일반 회원가입·로그인과 Kakao/Google/Naver OAuth 로그인이 모두 동일한 Access Token/Refresh Token 세션을 사용한다.

## 토큰 저장

- Access Token은 15분 만료 JWT이며 기존과 동일하게 JSON 응답의 `accessToken`으로 전달한다.
- Refresh Token은 64바이트 난수 기반 opaque token이며 14일 후 만료된다.
- Refresh Token 원문은 `HttpOnly`, `Secure`, `SameSite=None` 쿠키로만 브라우저에 전달한다.
- DB의 `REFRESH_TOKEN` 테이블에는 Refresh Token의 SHA-256 해시만 저장한다.
- 사용자별 여러 Refresh Token 행을 허용하여 여러 기기 로그인을 지원한다.

## 발급 및 회전

- 회원가입, 일반 로그인, OAuth 티켓 교환 성공 시 Access Token과 Refresh Token을 함께 발급한다.
- `POST /api/auth/refresh` 호출 시 기존 Refresh Token을 즉시 폐기하고 새로운 Access/Refresh Token 쌍을 발급한다.
- 이미 폐기된 Refresh Token이 다시 사용되면 탈취 가능성으로 판단하여 해당 사용자의 모든 Refresh Token을 폐기한다.

## 폐기

- `POST /api/auth/logout`은 현재 브라우저의 Refresh Token만 폐기하고 쿠키를 삭제한다.
- 비밀번호 변경, 비밀번호 재설정, 회원 탈퇴 시 사용자의 모든 Refresh Token을 폐기한다.
- Access Token은 서버 저장 없이 최대 15분 동안만 유효하며 별도 블랙리스트는 운영하지 않는다.

## CSRF 및 CORS

- Refresh Token 쿠키가 교차 사이트에서 동작해야 하므로 운영 환경에서는 `SameSite=None; Secure`를 사용한다.
- `/refresh`, `/logout`은 `X-Refresh-Request: true` 헤더를 필수로 요구한다. 브라우저의 CORS preflight와 허용 Origin 검사를 통과한 프론트만 호출할 수 있다.
- 프론트는 모든 인증 요청에 `credentials: include` 또는 Axios `withCredentials: true`를 사용한다.

## 로컬 환경

HTTP localhost에서는 Secure 쿠키가 저장되지 않으므로 로컬 백엔드 실행 환경에서만 다음 값을 사용한다.

```text
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
```
