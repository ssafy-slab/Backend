# SLAP Backend

여행지 탐색, 일정 관리, 리뷰, 날씨·주변 편의시설 정보를 제공하는 SLAP 서비스의 Spring Boot API 서버입니다.

## 주요 기능

- 이메일 회원가입·로그인과 JWT 인증
- Kakao, Google, Naver OAuth 로그인
- 프로필·비밀번호·회원 탈퇴 관리
- 여행지 검색 및 카테고리·지역 필터
- 여행지 평균 별점과 리뷰 수 조회
- 추천순·리뷰 많은 순·평점 높은 순 정렬
- 사용자당 장소별 리뷰 1개 등록·수정·삭제
- 마이페이지 내 리뷰 조회 및 10개 단위 페이지네이션
- 기상청 단기예보와 Kakao 주변 편의시설 조회
- 여행 일정·멤버·초대·스케줄 API

## 기술 스택

- Java 17
- Spring Boot 4
- Spring Security
- MyBatis
- MySQL
- JUnit 5, Mockito

## 환경 변수

필수:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/slap?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=utf8"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_db_password"
```

선택:

```powershell
$env:JWT_SECRET="change-me"
$env:KMA_SERVICE_KEY="your_kma_service_key"
$env:KAKAO_REST_API_KEY="your_kakao_rest_api_key"
$env:OAUTH_KAKAO_CLIENT_ID="..."
$env:OAUTH_KAKAO_CLIENT_SECRET="..."
$env:OAUTH_GOOGLE_CLIENT_ID="..."
$env:OAUTH_GOOGLE_CLIENT_SECRET="..."
$env:OAUTH_NAVER_CLIENT_ID="..."
$env:OAUTH_NAVER_CLIENT_SECRET="..."
```

외부 API 키가 없으면 해당 날씨·주변 시설·OAuth 기능만 제한됩니다.

## 실행과 테스트

```powershell
.\mvnw.cmd spring-boot:run
```

기본 주소는 `http://localhost:8080`입니다.

```powershell
.\mvnw.cmd test
```

Windows에서 Maven Wrapper 실행에 문제가 있으면 설치된 Maven의 `mvn test`를 사용할 수 있습니다.

## 주요 API

### 인증·사용자

| Method | Endpoint | 설명 |
|---|---|---|
| `POST` | `/api/auth/signup` | 회원가입 |
| `POST` | `/api/auth/login` | 로그인 |
| `GET` | `/api/oauth/{provider}/authorize` | OAuth 로그인 시작 |
| `GET` | `/api/users/me` | 내 정보 조회 |
| `PATCH` | `/api/users/me` | 프로필 수정 |
| `PATCH` | `/api/users/me/password` | 비밀번호 변경 |

### 여행지

```http
GET /api/places?category=관광지&regionId=1&keyword=서울&sort=recommended&page=0&size=20
```

정렬값:

- 생략: 기존 검색 관련도·기본 순서
- `recommended`: 보정 평점과 리뷰 수 기반 추천순
- `reviewCount`: 리뷰 많은 순
- `rating`: 평점 높은 순

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/places` | 여행지 검색·필터·정렬 |
| `GET` | `/api/places/{placeId}` | 여행지 상세 |
| `GET` | `/api/places/filters` | 카테고리·지역 필터 |
| `GET` | `/api/places/{placeId}/weather` | 날씨와 예보 |
| `GET` | `/api/places/{placeId}/nearby-facilities` | 주변 편의시설 |

### 리뷰

| Method | Endpoint | 설명 |
|---|---|---|
| `GET` | `/api/places/{placeId}/reviews` | 장소 리뷰·평균 별점 조회 |
| `POST` | `/api/places/{placeId}/reviews` | 내 리뷰 등록 |
| `PUT` | `/api/places/{placeId}/reviews/me` | 내 리뷰 수정 |
| `DELETE` | `/api/places/{placeId}/reviews/me` | 내 리뷰 삭제 |
| `GET` | `/api/users/me/reviews?page=0&size=10` | 내가 작성한 리뷰 |

별점은 1~5점 필수이며 리뷰 내용은 선택입니다. `(place_id, user_id)` 고유 제약으로 사용자당 장소별 리뷰 1개를 유지합니다.

## 프로젝트 구조

```text
src/main/java/com/ssafy/ssafy_slap
├─ auth
├─ user
├─ place
├─ review
├─ trip
├─ chat
└─ global

src/main/resources
├─ mapper
└─ schema/schema.sql
```
