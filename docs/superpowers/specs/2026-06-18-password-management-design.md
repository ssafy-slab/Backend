# Password Management Design

## Goal

로컬 계정 사용자가 마이페이지에서 현재 비밀번호를 확인한 뒤 비밀번호를 변경하고, 로그인하지 못한 경우 로그인 화면에서 이메일과 새 비밀번호를 입력해 비밀번호를 즉시 재설정할 수 있게 한다.

OAuth 전용 계정은 두 기능의 대상에서 제외한다.

## Existing Rules

- 로컬 계정 비밀번호는 BCrypt 해시로 저장한다.
- 유효한 비밀번호는 8자 이상 100자 이하이며 영문과 숫자를 각각 하나 이상 포함한다.
- OAuth 전용 계정은 `APP_USER.password_hash`가 `NULL`이다.
- 이메일은 앞뒤 공백을 제거하고 소문자로 정규화한다.

## User Experience

### My Page Password Change

마이페이지의 계정 설정 영역을 `기본 정보`와 `비밀번호 변경` 탭으로 나눈다.

비밀번호 변경 탭은 다음 필드를 제공한다.

- 현재 비밀번호
- 새 비밀번호
- 새 비밀번호 확인

새 비밀번호 입력 중에는 회원가입 화면과 동일한 검증 항목을 표시한다.

- 8자 이상
- 영문 포함
- 숫자 포함

현재 비밀번호가 맞고 새 비밀번호가 유효하며 확인 값이 일치하면 비밀번호를 변경한다. 성공 후 세 입력값을 비우고 완료 메시지를 표시한다. 현재 로그인 토큰은 유지한다.

OAuth 전용 계정은 비밀번호 변경 폼 대신 소셜 로그인 계정은 비밀번호를 변경할 수 없다는 안내를 표시한다.

### Forgot Password

로그인 화면의 `비밀번호 찾기` 버튼은 비밀번호 재설정 화면으로 이동한다.

재설정 화면은 다음 필드를 제공한다.

- 이메일
- 새 비밀번호
- 새 비밀번호 확인

새 비밀번호는 회원가입과 동일한 규칙으로 검증한다. 이메일에 해당하는 활성 로컬 계정이 존재하면 비밀번호를 즉시 변경하고 로그인 화면으로 돌아갈 수 있는 성공 상태를 표시한다.

활성 계정이 없거나 OAuth 전용 계정이면 재설정할 수 없다는 오류를 표시한다. MVP 로컬 환경의 명시적 요구에 따라 이메일 소유권 확인 토큰이나 메일 발송은 도입하지 않는다.

## Backend Architecture

### Password Validation

회원가입, 비밀번호 변경, 비밀번호 재설정 DTO에 동일한 Bean Validation 규칙을 적용한다.

- `@NotBlank`
- `@Size(min = 8, max = 100)`
- 영문과 숫자를 요구하는 `@Pattern`

프론트엔드 검증은 사용자 안내용이며 백엔드 검증을 최종 기준으로 삼는다.

### Change Password API

`PATCH /api/users/me/password`

JWT 인증이 필요하다.

Request:

```json
{
  "currentPassword": "oldPassword1",
  "newPassword": "newPassword2"
}
```

처리 순서:

1. JWT에서 현재 사용자 ID를 읽는다.
2. 활성 사용자를 조회한다.
3. `password_hash`가 `NULL`이면 OAuth 전용 계정으로 판단해 거부한다.
4. BCrypt로 현재 비밀번호를 검증한다.
5. 새 비밀번호를 BCrypt로 인코딩해 저장한다.

성공 시 `204 No Content`를 반환한다.

오류:

- 인증 없음: `401 Unauthorized`
- 현재 비밀번호 불일치: `400 Bad Request`
- OAuth 전용 계정: `400 Bad Request`
- 새 비밀번호 검증 실패: `400 Bad Request`
- 활성 사용자 없음: `404 Not Found`

### Reset Password API

`POST /api/auth/password/reset`

인증 없이 접근한다.

Request:

```json
{
  "email": "user@example.com",
  "newPassword": "newPassword2"
}
```

처리 순서:

1. 이메일을 정규화한다.
2. 활성 사용자를 이메일로 조회한다.
3. `password_hash`가 `NULL`이면 OAuth 전용 계정으로 판단해 거부한다.
4. 새 비밀번호를 BCrypt로 인코딩해 저장한다.

성공 시 `204 No Content`를 반환한다.

오류:

- 입력값 검증 실패: `400 Bad Request`
- 활성 로컬 계정 없음: `404 Not Found`
- OAuth 전용 계정: `400 Bad Request`

### Persistence

`UserMapper`에 활성 사용자의 비밀번호 해시를 갱신하는 단일 메서드를 추가한다.

```java
void updatePasswordHash(Long userId, String passwordHash);
```

SQL은 활성 사용자만 갱신하고 `updated_at`을 현재 시각으로 변경한다. 데이터베이스 스키마 변경은 필요하지 않다.

## Frontend Architecture

### API and Store

인증 API 모듈에 다음 요청 함수를 추가한다.

- `changePassword(token, payload)`
- `resetPassword(payload)`

인증 스토어는 마이페이지가 사용할 `changePassword` 액션을 제공한다. 비밀번호 재설정은 로그인 전 기능이므로 인증 상태를 변경하지 않고 API 함수를 직접 호출하거나 상태 비의존 스토어 액션으로 호출한다.

### OAuth Account Detection

현재 사용자 응답에는 계정 유형이 명시되어 있지 않다. 화면에서 합성 이메일 주소만으로 OAuth 여부를 추측하지 않도록 `AuthUserResponse`에 `localAccount` 불리언 값을 추가한다.

- `password_hash`가 존재하면 `true`
- `password_hash`가 `NULL`이면 `false`

이 값은 로그인, OAuth 로그인, 현재 사용자 조회, 프로필 수정 응답에서 동일하게 제공한다.

### Routing/View State

현재 SPA의 뷰 전환 구조에 `forgot-password` 뷰를 추가한다. 로그인 화면의 버튼이 이 뷰로 전환되고, 재설정 성공 후 로그인 뷰로 돌아갈 수 있다.

마이페이지 내부에서는 로컬 탭 상태로 `profile`과 `password`를 관리한다.

## Security and Scope

- 현재 비밀번호 원문은 저장하거나 로그로 출력하지 않는다.
- API 응답에 비밀번호 또는 해시를 포함하지 않는다.
- 새 비밀번호 확인 값은 프론트엔드에서만 사용하며 API로 전송하지 않는다.
- 비밀번호 변경 후 기존 JWT를 강제 폐기하지 않는다. 현재 구현에 토큰 취소 저장소가 없으므로 MVP 범위에서는 로그인 상태를 유지한다.
- 비밀번호 재설정 API는 이메일 소유권을 검증하지 않는다. 이는 MVP 로컬 환경에서만 허용되는 의도적인 제한이다.
- 이메일 존재 여부가 오류 코드로 드러나는 계정 열거 위험도 같은 이유로 MVP 범위에서 수용한다.

## Testing

### Backend

- 현재 비밀번호가 맞으면 새 BCrypt 해시를 저장한다.
- 현재 비밀번호가 틀리면 변경하지 않는다.
- OAuth 전용 계정은 비밀번호를 변경할 수 없다.
- 이메일에 해당하는 활성 로컬 계정의 비밀번호를 재설정한다.
- 존재하지 않는 이메일은 재설정하지 않는다.
- OAuth 전용 계정은 재설정하지 않는다.
- 두 요청 DTO가 회원가입과 동일한 비밀번호 규칙을 거부·허용한다.
- MyBatis XML에 비밀번호 갱신 SQL이 올바르게 연결된다.

### Frontend

- TypeScript 검사와 프로덕션 빌드가 통과한다.
- 로그인 화면에서 비밀번호 찾기 화면으로 이동할 수 있다.
- 비밀번호 확인 불일치와 규칙 위반 시 API를 호출하지 않는다.
- 마이페이지 변경 요청에 현재 비밀번호와 새 비밀번호가 전달된다.
- OAuth 전용 계정에는 비밀번호 변경 폼 대신 안내가 표시된다.

### Manual Verification

- 로컬 계정으로 로그인해 비밀번호를 변경한 뒤 새 비밀번호로 재로그인한다.
- 이전 비밀번호로 로그인이 거부되는지 확인한다.
- 로그아웃 상태에서 비밀번호를 재설정하고 새 비밀번호로 로그인한다.
- OAuth 계정에서 변경 및 재설정이 거부되는지 확인한다.

