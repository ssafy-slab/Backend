# 프론트엔드 전달용 프롬프트

현재 백엔드의 모든 로그인 방식(회원가입, 이메일 로그인, Kakao/Google/Naver OAuth)에 Access Token + HttpOnly Refresh Token 방식이 적용되었다. 아래 API 계약에 맞게 프론트 인증 구조를 수정해줘.

## 백엔드 계약

1. 다음 API의 JSON 응답 형식은 기존과 같다.
   - `POST /api/auth/signup`
   - `POST /api/auth/login`
   - `POST /api/oauth/token`

```ts
type AuthResponse = {
  tokenType: "Bearer";
  accessToken: string;
  user: {
    userId: number;
    email: string;
    nickname: string;
    role: string;
    oauthUser: boolean;
  };
};
```

2. Refresh Token은 응답 JSON에 존재하지 않는다. 백엔드가 `slap_refresh_token`이라는 HttpOnly 쿠키로 저장하므로 프론트에서 읽거나 localStorage/sessionStorage에 저장하려 하지 마라.

3. 인증 관련 요청을 포함한 API 클라이언트에 반드시 쿠키 전송 옵션을 적용한다.
   - Axios: `withCredentials: true`
   - fetch: `credentials: "include"`

4. Access Token 재발급 API:

```http
POST /api/auth/refresh
X-Refresh-Request: true
```

- 요청 body 없음
- 성공 시 새로운 `AuthResponse` 반환
- Refresh Token 쿠키도 자동으로 새 토큰으로 교체됨
- Refresh Token이 없거나 만료·폐기된 경우 `401`

5. 로그아웃 API:

```http
POST /api/auth/logout
X-Refresh-Request: true
```

- 성공 시 `204`
- 서버 세션 폐기 및 Refresh Token 쿠키 삭제

## 구현 요구사항

- Access Token은 가능하면 React/Vue 상태 관리 또는 메모리에 보관한다. Refresh Token은 어떤 JavaScript 저장소에도 저장하지 않는다.
- 앱 최초 진입 또는 새로고침 시 Access Token이 메모리에 없다면 `/api/auth/refresh`를 한 번 호출해 로그인 상태를 복구한다.
- API 요청 시 최신 Access Token을 `Authorization: Bearer {accessToken}` 헤더에 넣는다.
- 일반 API가 `401`을 반환하면 `/api/auth/refresh`를 한 번 호출한다. 성공하면 새 Access Token으로 원래 요청을 딱 한 번 재시도한다.
- 여러 API가 동시에 `401`을 반환해도 refresh 요청은 하나만 실행하고 나머지 요청은 같은 Promise를 기다리도록 single-flight/queue 처리를 구현한다.
- 재시도 요청에 `_retry` 같은 플래그를 넣어 무한 재발급 루프를 방지한다.
- `/api/auth/login`, `/api/auth/signup`, `/api/auth/refresh`, `/api/auth/logout`, `/api/oauth/token` 요청 자체에는 401 interceptor 재발급 로직을 적용하지 않는다.
- Refresh 실패 시 Access Token과 사용자 상태를 모두 비우고 로그인 화면으로 이동한다.
- 로그아웃은 서버 `/api/auth/logout` 성공 여부와 관계없이 최종적으로 로컬 Access Token과 사용자 상태를 비운다.
- OAuth 콜백에서는 URL의 일회용 `ticket`을 `POST /api/oauth/token`으로 교환할 때도 반드시 쿠키 전송 옵션을 사용한다. 성공 후 URL에서 ticket query parameter를 제거한다.
- WebSocket 연결 시에도 가장 최근 Access Token을 사용하고, 토큰 갱신 후 재연결이 필요하면 새 토큰으로 연결한다.
- Refresh Token이나 쿠키 값을 console.log, 에러 로깅, 분석 도구에 기록하지 않는다.

## 완료 조건

- 이메일 로그인과 모든 OAuth 로그인 후 새로고침해도 로그인 상태가 복구된다.
- Access Token 만료 후 사용자가 인지하지 못하게 한 번 자동 갱신되고 원래 API가 성공한다.
- 동시 401 상황에서도 refresh API가 한 번만 호출된다.
- Refresh 실패 시 무한 루프 없이 로그인 화면으로 이동한다.
- 로그아웃 후 `/api/auth/refresh`가 `401`을 반환한다.
- 프론트 저장소와 네트워크 JSON 응답 어디에도 Refresh Token 원문이 노출되지 않는다.
