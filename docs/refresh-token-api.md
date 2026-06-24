# Refresh Token API

## 공통 로그인 응답

회원가입, 일반 로그인, OAuth 티켓 교환 응답 JSON은 기존 형식을 유지한다.

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJ...",
  "user": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "traveler",
    "role": "USER",
    "oauthUser": false
  }
}
```

Refresh Token은 JSON에 포함되지 않고 다음 쿠키로만 전달된다.

```text
Set-Cookie: slap_refresh_token=...; Path=/api/auth; Max-Age=1209600; Secure; HttpOnly; SameSite=None
```

## Access Token 재발급

```http
POST /api/auth/refresh
X-Refresh-Request: true
Cookie: slap_refresh_token=...
```

- 요청 본문 없음
- 성공: `200 OK`, 새로운 로그인 응답과 회전된 Refresh Token 쿠키
- 실패: `401 Unauthorized`
- 커스텀 헤더 누락: `400 Bad Request`

## 로그아웃

```http
POST /api/auth/logout
X-Refresh-Request: true
Cookie: slap_refresh_token=...
```

- 성공: `204 No Content`
- 현재 Refresh Token을 서버에서 폐기하고 쿠키를 즉시 만료시킨다.

## 배포

운영 DB에 [2026-06-24-refresh-tokens.sql](sql/2026-06-24-refresh-tokens.sql)을 먼저 적용한다.

운영 환경:

```text
JWT_ACCESS_TOKEN_VALIDITY_MILLIS=900000
JWT_REFRESH_TOKEN_VALIDITY_MILLIS=1209600000
AUTH_REFRESH_COOKIE_SECURE=true
AUTH_REFRESH_COOKIE_SAME_SITE=None
```

HTTP 로컬 개발 환경:

```text
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
```
