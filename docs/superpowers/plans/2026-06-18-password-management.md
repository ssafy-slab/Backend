# Password Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 로컬 계정 사용자가 현재 비밀번호를 검증해 비밀번호를 변경하고, 로그아웃 상태에서는 이메일과 새 비밀번호로 비밀번호를 재설정할 수 있게 한다.

**Architecture:** 백엔드는 기존 `APP_USER.password_hash`와 BCrypt를 그대로 사용하며 인증된 변경 API와 공개 재설정 API를 분리한다. `AuthUserResponse.localAccount`로 OAuth 전용 계정을 명시하고, 프론트엔드는 마이페이지 탭과 별도 비밀번호 찾기 화면에서 공통 비밀번호 규칙을 사용한다.

**Tech Stack:** Java 17, Spring Boot, Spring Security BCrypt, Bean Validation, MyBatis, JUnit 5, Mockito, Vue 3, TypeScript, Pinia, Vitest, Vue Test Utils

---

## File Map

### Backend

- Create `src/main/java/com/ssafy/ssafy_slap/auth/dto/PasswordResetRequest.java`: 공개 비밀번호 재설정 입력과 서버 검증 규칙.
- Create `src/main/java/com/ssafy/ssafy_slap/user/dto/PasswordChangeRequest.java`: 로그인 사용자의 현재/새 비밀번호 입력.
- Create `src/test/java/com/ssafy/ssafy_slap/auth/dto/PasswordRequestValidationTest.java`: 두 DTO의 공통 비밀번호 정책 검증.
- Modify `src/main/java/com/ssafy/ssafy_slap/auth/dto/AuthUserResponse.java`: `localAccount` 계정 유형 노출.
- Create `src/test/java/com/ssafy/ssafy_slap/auth/dto/AuthUserResponseTest.java`: 로컬/OAuth 계정 유형 응답 검증.
- Modify `src/main/java/com/ssafy/ssafy_slap/auth/controller/AuthController.java`: 공개 재설정 엔드포인트.
- Modify `src/main/java/com/ssafy/ssafy_slap/auth/controller/OAuthController.java`: OAuth 콜백 fragment에 계정 유형 포함.
- Modify `src/main/java/com/ssafy/ssafy_slap/auth/service/AuthService.java`: 이메일 기반 로컬 계정 재설정.
- Modify `src/main/java/com/ssafy/ssafy_slap/user/controller/UserController.java`: 인증된 비밀번호 변경 엔드포인트.
- Modify `src/main/java/com/ssafy/ssafy_slap/user/service/UserService.java`: 현재 비밀번호 검증 및 새 해시 저장.
- Modify `src/main/java/com/ssafy/ssafy_slap/user/mapper/UserMapper.java`: 비밀번호 해시 갱신 계약.
- Modify `src/main/resources/mapper/user/UserMapper.xml`: 활성 사용자 비밀번호 해시 갱신 SQL.
- Modify `src/test/java/com/ssafy/ssafy_slap/auth/service/AuthServiceTest.java`: 재설정 성공·실패 테스트.
- Modify `src/test/java/com/ssafy/ssafy_slap/user/service/UserServiceTest.java`: 변경 성공·현재 비밀번호 오류·OAuth 거부 테스트.
- Modify `src/test/java/com/ssafy/ssafy_slap/user/mapper/UserMapperXmlTest.java`: SQL 연결 테스트.

### Frontend

- Create `src/shared/lib/password.ts`: 회원가입·변경·재설정에서 공유할 비밀번호 검사.
- Create `src/shared/lib/password.test.ts`: 공통 비밀번호 검사 단위 테스트.
- Create `src/pages/auth/ui/ForgotPasswordPage.vue`: 이메일과 새 비밀번호를 받는 재설정 화면.
- Create `src/pages/auth/ui/ForgotPasswordPage.test.ts`: 검증 실패와 성공 요청 UI 테스트.
- Modify `package.json` and `pnpm-lock.yaml`: Vitest, Vue Test Utils, jsdom과 `test` 스크립트.
- Modify `vite.config.ts`: Vitest jsdom 환경과 Vue 설정.
- Modify `src/entities/auth/api/authApi.ts`: 계정 유형과 두 비밀번호 API.
- Modify `src/stores/auth.ts`: 인증된 비밀번호 변경 액션.
- Modify `src/features/auth/ui/AuthForm.vue`: 공통 규칙 사용과 비밀번호 찾기 뷰 연결.
- Modify `src/pages/profile/ui/ProfilePage.vue`: 기본 정보/비밀번호 변경 탭 및 OAuth 안내.
- Modify `src/app/App.vue`: `forgot-password` 뷰와 OAuth `localAccount` 파싱.

---

### Task 1: Backend Password Request Validation

**Files:**

- Create: `src/main/java/com/ssafy/ssafy_slap/auth/dto/PasswordResetRequest.java`
- Create: `src/main/java/com/ssafy/ssafy_slap/user/dto/PasswordChangeRequest.java`
- Create: `src/test/java/com/ssafy/ssafy_slap/auth/dto/PasswordRequestValidationTest.java`

- [ ] **Step 1: Write failing validation tests**

```java
class PasswordRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsResetPasswordWithoutNumber() {
        var violations = validator.validate(
                new PasswordResetRequest("test@example.com", "password")
        );
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
    }

    @Test
    void acceptsValidChangePassword() {
        var violations = validator.validate(
                new PasswordChangeRequest("oldPassword1", "newPassword2")
        );
        assertThat(violations).isEmpty();
    }
}
```

- [ ] **Step 2: Verify RED**

Run: `.\mvnw.cmd -Dtest=PasswordRequestValidationTest test`

Expected: compilation fails because both request records do not exist.

- [ ] **Step 3: Add request records with the existing password policy**

```java
public record PasswordResetRequest(
        @Email @NotBlank String email,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must contain letters and numbers")
        String newPassword
) {}
```

```java
public record PasswordChangeRequest(
        @NotBlank String currentPassword,
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "password must contain letters and numbers")
        String newPassword
) {}
```

- [ ] **Step 4: Verify GREEN**

Run: `.\mvnw.cmd -Dtest=PasswordRequestValidationTest test`

Expected: all password request validation tests pass.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/ssafy/ssafy_slap/auth/dto/PasswordResetRequest.java src/main/java/com/ssafy/ssafy_slap/user/dto/PasswordChangeRequest.java src/test/java/com/ssafy/ssafy_slap/auth/dto/PasswordRequestValidationTest.java
git commit -m "test: define password request validation"
```

### Task 2: Backend Password Persistence and Account Type

**Files:**

- Modify: `src/main/java/com/ssafy/ssafy_slap/auth/dto/AuthUserResponse.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/user/mapper/UserMapper.java`
- Modify: `src/main/resources/mapper/user/UserMapper.xml`
- Modify: `src/test/java/com/ssafy/ssafy_slap/user/mapper/UserMapperXmlTest.java`

- [ ] **Step 1: Write failing mapper and response tests**

Add assertions that the mapper XML contains:

```java
assertThat(mapperXml).contains("<update id=\"updatePasswordHash\">");
assertThat(mapperXml).contains("password_hash = #{passwordHash}");
assertThat(mapperXml).contains("AND status = 'ACTIVE'");
```

Add `AuthUserResponseTest` coverage:

```java
assertThat(AuthUserResponse.from(localUser).localAccount()).isTrue();
assertThat(AuthUserResponse.from(oauthUser).localAccount()).isFalse();
```

- [ ] **Step 2: Verify RED**

Run: `.\mvnw.cmd -Dtest=UserMapperXmlTest,AuthUserResponseTest test`

Expected: mapper assertions fail and `localAccount()` is missing.

- [ ] **Step 3: Implement the mapper and response shape**

Extend the response:

```java
public record AuthUserResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        boolean localAccount
) {
    public static AuthUserResponse from(AppUser user) {
        return new AuthUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getPasswordHash() != null
        );
    }
}
```

Add the mapper contract:

```java
void updatePasswordHash(
        @Param("userId") Long userId,
        @Param("passwordHash") String passwordHash
);
```

Add the SQL:

```xml
<update id="updatePasswordHash">
    UPDATE APP_USER
    SET password_hash = #{passwordHash},
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = #{userId}
      AND status = 'ACTIVE'
</update>
```

- [ ] **Step 4: Verify GREEN**

Run: `.\mvnw.cmd -Dtest=UserMapperXmlTest,AuthUserResponseTest test`

Expected: both test classes pass.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/ssafy/ssafy_slap/auth/dto/AuthUserResponse.java src/main/java/com/ssafy/ssafy_slap/user/mapper/UserMapper.java src/main/resources/mapper/user/UserMapper.xml src/test/java/com/ssafy/ssafy_slap/auth/dto/AuthUserResponseTest.java src/test/java/com/ssafy/ssafy_slap/user/mapper/UserMapperXmlTest.java
git commit -m "feat: expose local account type and password update"
```

### Task 3: Authenticated Password Change

**Files:**

- Modify: `src/test/java/com/ssafy/ssafy_slap/user/service/UserServiceTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/user/service/UserService.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/user/controller/UserController.java`

- [ ] **Step 1: Write failing service tests**

Construct `UserService` with `PasswordEncoder` and test these behaviors:

```java
when(userMapper.findActiveById(1L)).thenReturn(Optional.of(localUser));
when(passwordEncoder.matches("oldPassword1", "encoded-old")).thenReturn(true);
when(passwordEncoder.encode("newPassword2")).thenReturn("encoded-new");

userService.changePassword(1L, new PasswordChangeRequest("oldPassword1", "newPassword2"));

verify(userMapper).updatePasswordHash(1L, "encoded-new");
```

Also assert:

- current password mismatch throws `ResponseStatusException` containing `Current password is incorrect`;
- `passwordHash == null` throws an error containing `OAuth account`;
- neither failure calls `updatePasswordHash`.

- [ ] **Step 2: Verify RED**

Run: `.\mvnw.cmd -Dtest=UserServiceTest test`

Expected: compilation fails because `PasswordEncoder` constructor injection and `changePassword` do not exist.

- [ ] **Step 3: Implement minimal service behavior**

```java
@Transactional
public void changePassword(Long userId, PasswordChangeRequest request) {
    AppUser user = findActiveUser(userId);
    if (user.getPasswordHash() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth account cannot change password");
    }
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
    userMapper.updatePasswordHash(userId, passwordEncoder.encode(request.newPassword()));
}
```

Inject `PasswordEncoder` through the existing constructor and update tests to pass the mock.

- [ ] **Step 4: Add the controller endpoint**

```java
@PatchMapping("/password")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void changePassword(
        Authentication authentication,
        @Valid @RequestBody PasswordChangeRequest request
) {
    userService.changePassword(currentUserId(authentication), request);
}
```

- [ ] **Step 5: Verify GREEN**

Run: `.\mvnw.cmd -Dtest=UserServiceTest test`

Expected: nickname, account deletion, and password change tests all pass.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/ssafy/ssafy_slap/user/controller/UserController.java src/main/java/com/ssafy/ssafy_slap/user/service/UserService.java src/test/java/com/ssafy/ssafy_slap/user/service/UserServiceTest.java
git commit -m "feat: change local account password"
```

### Task 4: Public Password Reset

**Files:**

- Modify: `src/test/java/com/ssafy/ssafy_slap/auth/service/AuthServiceTest.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/auth/service/AuthService.java`
- Modify: `src/main/java/com/ssafy/ssafy_slap/auth/controller/AuthController.java`

- [ ] **Step 1: Write failing reset tests**

Test the successful path:

```java
when(userMapper.findActiveByEmail("test@example.com")).thenReturn(Optional.of(localUser));
when(passwordEncoder.encode("newPassword2")).thenReturn("encoded-new");

authService.resetPassword(new PasswordResetRequest(" TEST@example.com ", "newPassword2"));

verify(userMapper).updatePasswordHash(10L, "encoded-new");
```

Add failures:

- unknown email throws `404` and does not update;
- user with `passwordHash == null` throws `400` containing `OAuth account`;
- failed paths never call `passwordEncoder.encode`.

- [ ] **Step 2: Verify RED**

Run: `.\mvnw.cmd -Dtest=AuthServiceTest test`

Expected: compilation fails because `resetPassword` does not exist.

- [ ] **Step 3: Implement reset service**

```java
@Transactional
public void resetPassword(PasswordResetRequest request) {
    AppUser user = userMapper.findActiveByEmail(normalizeEmail(request.email()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Local account not found"));
    if (user.getPasswordHash() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OAuth account cannot reset password");
    }
    userMapper.updatePasswordHash(user.getUserId(), passwordEncoder.encode(request.newPassword()));
}
```

- [ ] **Step 4: Add the public endpoint**

```java
@PostMapping("/password/reset")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
    authService.resetPassword(request);
}
```

- [ ] **Step 5: Verify GREEN**

Run: `.\mvnw.cmd -Dtest=AuthServiceTest test`

Expected: all signup, login, and reset tests pass.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/ssafy/ssafy_slap/auth/controller/AuthController.java src/main/java/com/ssafy/ssafy_slap/auth/service/AuthService.java src/test/java/com/ssafy/ssafy_slap/auth/service/AuthServiceTest.java
git commit -m "feat: reset local account password"
```

### Task 5: OAuth Callback Account Type

**Files:**

- Modify: `src/main/java/com/ssafy/ssafy_slap/auth/controller/OAuthController.java`
- Create: `src/test/java/com/ssafy/ssafy_slap/auth/controller/OAuthControllerTest.java`

- [ ] **Step 1: Write a failing callback redirect test**

Mock an OAuth response whose user has `localAccount=false`, call the callback with matching state cookie, and assert:

```java
assertThat(response.getHeaders().getLocation().getFragment())
        .contains("localAccount=false");
```

- [ ] **Step 2: Verify RED**

Run: `.\mvnw.cmd -Dtest=OAuthControllerTest test`

Expected: redirect fragment does not contain `localAccount`.

- [ ] **Step 3: Add the fragment field**

Append:

```java
+ "&localAccount=" + response.user().localAccount()
```

to `successFragment`.

- [ ] **Step 4: Verify GREEN**

Run: `.\mvnw.cmd -Dtest=OAuthControllerTest test`

Expected: callback test passes.

- [ ] **Step 5: Run backend regression suite**

Run: `.\mvnw.cmd test`

Expected: Maven exits with code 0 and reports zero failures/errors.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/ssafy/ssafy_slap/auth/controller/OAuthController.java src/test/java/com/ssafy/ssafy_slap/auth/controller/OAuthControllerTest.java
git commit -m "feat: include account type in oauth callback"
```

### Task 6: Frontend Password Rules and Test Harness

**Files:**

- Modify: `package.json`
- Modify: `pnpm-lock.yaml`
- Modify: `vite.config.ts`
- Create: `src/shared/lib/password.ts`
- Create: `src/shared/lib/password.test.ts`
- Modify: `src/features/auth/ui/AuthForm.vue`

- [ ] **Step 1: Add the frontend test dependencies**

Run:

```powershell
corepack pnpm add -D vitest @vue/test-utils jsdom
```

Add:

```json
"test": "vitest run"
```

and configure `vite.config.ts` with:

```ts
test: {
  environment: 'jsdom',
},
```

- [ ] **Step 2: Write failing password helper tests**

```ts
describe('getPasswordChecks', () => {
  it('requires length, a letter, and a number', () => {
    expect(getPasswordChecks('password')).toEqual([
      { label: '8자 이상', valid: true },
      { label: '영문 포함', valid: true },
      { label: '숫자 포함', valid: false },
    ])
  })

  it('accepts a valid password', () => {
    expect(isPasswordValid('password1')).toBe(true)
  })
})
```

- [ ] **Step 3: Verify RED**

Run: `corepack pnpm test -- src/shared/lib/password.test.ts`

Expected: module import fails because `password.ts` does not exist.

- [ ] **Step 4: Implement and reuse the helper**

```ts
export function getPasswordChecks(password: string) {
  return [
    { label: '8자 이상', valid: password.length >= 8 },
    { label: '영문 포함', valid: /[A-Za-z]/.test(password) },
    { label: '숫자 포함', valid: /\d/.test(password) },
  ]
}

export function isPasswordValid(password: string) {
  return getPasswordChecks(password).every((item) => item.valid)
}
```

Replace the duplicate checks in `AuthForm.vue` with this helper.

- [ ] **Step 5: Verify GREEN**

Run: `corepack pnpm test -- src/shared/lib/password.test.ts`

Expected: helper tests pass.

- [ ] **Step 6: Commit in the frontend repository**

```powershell
git add package.json pnpm-lock.yaml vite.config.ts src/shared/lib/password.ts src/shared/lib/password.test.ts src/features/auth/ui/AuthForm.vue
git commit -m "test: share password validation rules"
```

### Task 7: Frontend API and Auth State

**Files:**

- Modify: `src/entities/auth/api/authApi.ts`
- Modify: `src/stores/auth.ts`
- Modify: `src/app/App.vue`

- [ ] **Step 1: Extend types and API functions**

Add:

```ts
export type AuthUser = {
  userId: number
  email: string
  nickname: string
  role: string
  localAccount: boolean
}

export type PasswordChangePayload = {
  currentPassword: string
  newPassword: string
}

export type PasswordResetPayload = {
  email: string
  newPassword: string
}
```

Add `changePassword` using `PATCH /api/users/me/password` and `resetPassword` using `POST /api/auth/password/reset`. Both accept `204` and map `400`, `404`, and `401` to Korean user-facing messages.

- [ ] **Step 2: Add store action**

```ts
async function changePassword(payload: PasswordChangePayload) {
  if (!accessToken.value) throw new Error('로그인이 필요합니다.')
  await authApi.changePassword(accessToken.value, payload)
}
```

Export the action. Keep reset password state-independent.

- [ ] **Step 3: Parse OAuth account type**

In `handleOAuthCallback`, require `localAccount` and construct:

```ts
localAccount: params.get('localAccount') === 'true',
```

- [ ] **Step 4: Verify type safety**

Run: `corepack pnpm type-check`

Expected: TypeScript exits with code 0.

- [ ] **Step 5: Commit**

```powershell
git add src/entities/auth/api/authApi.ts src/stores/auth.ts src/app/App.vue
git commit -m "feat: add password management api client"
```

### Task 8: Forgot Password Page

**Files:**

- Create: `src/pages/auth/ui/ForgotPasswordPage.vue`
- Create: `src/pages/auth/ui/ForgotPasswordPage.test.ts`
- Modify: `src/features/auth/ui/AuthForm.vue`
- Modify: `src/app/App.vue`

- [ ] **Step 1: Write failing component tests**

Mount the page and test:

```ts
it('does not submit mismatched passwords', async () => {
  await wrapper.get('input[type="email"]').setValue('test@example.com')
  await wrapper.get('[data-test="new-password"]').setValue('password1')
  await wrapper.get('[data-test="password-confirm"]').setValue('password2')
  await wrapper.get('form').trigger('submit')
  expect(resetPassword).not.toHaveBeenCalled()
  expect(wrapper.text()).toContain('비밀번호 확인이 일치하지 않습니다.')
})
```

Add a success test that expects:

```ts
expect(resetPassword).toHaveBeenCalledWith({
  email: 'test@example.com',
  newPassword: 'password1',
})
expect(wrapper.text()).toContain('비밀번호가 재설정되었습니다.')
```

- [ ] **Step 2: Verify RED**

Run: `corepack pnpm test -- src/pages/auth/ui/ForgotPasswordPage.test.ts`

Expected: component import fails because the page does not exist.

- [ ] **Step 3: Implement the page**

Use `getPasswordChecks` and `isPasswordValid`; include email, new password, confirmation, error/success text, a submit button, and a `change('login')` button.

- [ ] **Step 4: Wire the view**

Add `'forgot-password'` to `ViewName`, render `ForgotPasswordPage` for that state, and change the login form button to:

```vue
<button type="button" @click="emit('change', 'forgot-password')">비밀번호 찾기</button>
```

- [ ] **Step 5: Verify GREEN**

Run: `corepack pnpm test -- src/pages/auth/ui/ForgotPasswordPage.test.ts`

Expected: mismatch and successful reset tests pass.

- [ ] **Step 6: Commit**

```powershell
git add src/pages/auth/ui/ForgotPasswordPage.vue src/pages/auth/ui/ForgotPasswordPage.test.ts src/features/auth/ui/AuthForm.vue src/app/App.vue
git commit -m "feat: add forgot password flow"
```

### Task 9: My Page Password Change Tab

**Files:**

- Create: `src/pages/profile/ui/ProfilePage.test.ts`
- Modify: `src/pages/profile/ui/ProfilePage.vue`

- [ ] **Step 1: Write failing profile tests**

Test a local account:

```ts
expect(wrapper.text()).toContain('비밀번호 변경')
await wrapper.get('[data-test="password-tab"]').trigger('click')
await wrapper.get('[data-test="current-password"]').setValue('oldPassword1')
await wrapper.get('[data-test="new-password"]').setValue('newPassword2')
await wrapper.get('[data-test="password-confirm"]').setValue('newPassword2')
await wrapper.get('[data-test="change-password-form"]').trigger('submit')
expect(changePassword).toHaveBeenCalledWith({
  currentPassword: 'oldPassword1',
  newPassword: 'newPassword2',
})
```

Test an OAuth account:

```ts
expect(wrapper.text()).toContain('소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.')
expect(wrapper.find('[data-test="change-password-form"]').exists()).toBe(false)
```

- [ ] **Step 2: Verify RED**

Run: `corepack pnpm test -- src/pages/profile/ui/ProfilePage.test.ts`

Expected: password tab/form selectors are absent.

- [ ] **Step 3: Implement tab state and form**

Add:

```ts
const activeTab = ref<'profile' | 'password'>('profile')
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  passwordConfirm: '',
})
```

On submit:

1. require all fields;
2. require `isPasswordValid(newPassword)`;
3. require matching confirmation;
4. call `authStore.changePassword`;
5. clear all password fields;
6. emit `saved('비밀번호가 변경되었습니다.')`.

For `currentUser.localAccount === false`, show only the OAuth guidance in the password tab.
Extend the page's local `User` prop type with `localAccount: boolean` so it matches `AuthUser`.

- [ ] **Step 4: Verify GREEN**

Run: `corepack pnpm test -- src/pages/profile/ui/ProfilePage.test.ts`

Expected: local submission and OAuth guidance tests pass.

- [ ] **Step 5: Run frontend regression checks**

Run:

```powershell
corepack pnpm test
corepack pnpm type-check
corepack pnpm build
```

Expected: all tests pass and both type-check and build exit with code 0.

- [ ] **Step 6: Commit**

```powershell
git add src/pages/profile/ui/ProfilePage.vue src/pages/profile/ui/ProfilePage.test.ts
git commit -m "feat: add my page password change"
```

### Task 10: End-to-End Verification

**Files:**

- No source files unless verification exposes a defect.

- [ ] **Step 1: Verify backend**

Run from `Backend`:

```powershell
.\mvnw.cmd test
```

Expected: zero failures and zero errors.

- [ ] **Step 2: Verify frontend**

Run from `slap-frontend`:

```powershell
corepack pnpm test
corepack pnpm type-check
corepack pnpm build
```

Expected: all commands exit with code 0.

- [ ] **Step 3: Verify the local user flow in the running app**

Use a local account to:

1. log in;
2. open My Page → Password Change;
3. confirm the wrong current password is rejected;
4. change to a valid new password;
5. log out;
6. confirm the old password fails;
7. confirm the new password logs in;
8. log out and use Forgot Password;
9. confirm the newly reset password logs in.

- [ ] **Step 4: Verify OAuth exclusions**

With an OAuth account:

1. open My Page → Password Change and confirm only the exclusion notice appears;
2. enter the OAuth account email on Forgot Password and confirm reset is rejected.

- [ ] **Step 5: Inspect repository state**

Run in both repositories:

```powershell
git status --short --branch
git log -5 --oneline
```

Expected: only intentional changes/commits are present and no generated build artifacts are tracked.
