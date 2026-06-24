# Community Bookmark API

모든 찜 API는 JWT 인증이 필요하다. 좋아요와 찜은 별도 데이터로 저장된다.

## 게시글 찜

```http
POST /api/community/posts/{postId}/bookmark
```

- 성공: `204 No Content`
- 이미 찜한 게시글에 다시 요청해도 성공한다.
- 존재하지 않거나 삭제된 게시글: `404 Not Found`

## 게시글 찜 해제

```http
DELETE /api/community/posts/{postId}/bookmark
```

- 성공: `204 No Content`
- 이미 해제된 게시글에 다시 요청해도 성공한다.
- 존재하지 않거나 삭제된 게시글: `404 Not Found`

## 내 찜 목록

```http
GET /api/users/me/bookmarked-community-posts?page=0&size=20
```

- 최신 찜 순으로 활성 게시글만 반환한다.
- `page` 기본값은 `0`, `size` 기본값은 `20`, 최대값은 `50`이다.
- 응답은 기존 `CommunityPostSummaryResponse[]` 형식이다.

커뮤니티 게시글 목록·상세 응답에는 다음 필드가 추가된다.

```json
{
  "bookmarkedByMe": true
}
```

비로그인 커뮤니티 조회에서는 `false`이며, 내 찜 목록에서는 항상 `true`다.
