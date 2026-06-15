# SLAP Backend

Spring Boot, MyBatis, MySQL 기반의 SLAP 여행 서비스 백엔드입니다.

## 주요 기능

- 여행지 목록/상세 조회
- 카테고리, 지역, 키워드 기반 여행지 검색
- 탐색 필터용 카테고리/지역 조회
- 여행지 좌표 기반 기상청 단기예보 조회
- 이미지 URL 목적별 응답
  - 목록용 썸네일: `thumbnailImageUrl`
  - 상세용 원본 이미지: `detailImageUrl`

## 환경 변수

실행 전에 다음 환경변수를 설정해야 합니다.

```powershell
$env:DB_URL="jdbc:mysql://host:3306/database?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=utf8"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_db_password"
$env:KMA_SERVICE_KEY="your_kma_service_key"
```

- `DB_URL`: MySQL JDBC URL
- `DB_USERNAME`: DB 사용자명
- `DB_PASSWORD`: DB 비밀번호
- `KMA_SERVICE_KEY`: 공공데이터포털 기상청 단기예보 조회서비스 인증키

`KMA_SERVICE_KEY`가 비어 있으면 서버는 정상 실행되지만, 날씨 API는 `available=false`로 응답합니다.

## 실행

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

서버 기본 주소는 `http://localhost:8080`입니다.

## 테스트

```powershell
.\mvnw.cmd test
```

## 주요 API

### 여행지 목록 조회

```http
GET /api/places?page=0&size=20
```

선택 query parameter:

- `category`: 카테고리
- `regionId`: 지역 ID
- `keyword`: 검색어
- `page`: 페이지 번호
- `size`: 페이지 크기

기본 정렬은 `place_id ASC`입니다. 키워드 검색 시에는 검색 점수 정렬이 먼저 적용되고, 같은 점수에서는 `place_id ASC`가 적용됩니다.

### 여행지 상세 조회

```http
GET /api/places/{placeId}
```

### 탐색 필터 조회

```http
GET /api/places/filters
```

카테고리와 시/도, 시/군/구 지역 목록을 반환합니다.

### 여행지 날씨 조회

```http
GET /api/places/{placeId}/weather
```

place의 위도/경도를 기상청 격자 좌표로 변환한 뒤 단기예보를 조회합니다.

응답 예시:

```json
{
  "available": true,
  "temperature": 32,
  "feelsLikeTemperature": 32.0,
  "precipitationProbability": 0,
  "humidity": 35,
  "windSpeed": 0.9,
  "precipitationType": "없음",
  "skyStatus": "맑음",
  "precipitationOneHour": "강수없음",
  "updatedAt": "2026-06-15T14:00:00"
}
```

외부 API 호출 실패, 인증키 누락, 좌표 누락 시에는 `available=false`와 안내 메시지를 반환합니다.
