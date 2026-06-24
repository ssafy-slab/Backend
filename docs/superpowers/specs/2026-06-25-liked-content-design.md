# Liked Content Unification Design

## Goal

Remove the accidental community bookmark feature, preserve its user data as community post likes, add persistent place likes, and expose both liked posts and liked places from one My Page "좋아요" tab.

## Data Migration

Before dropping `COMMUNITY_POST_BOOKMARK`, copy every `(post_id, user_id, created_at)` row into `COMMUNITY_POST_LIKE`. Duplicate post/user pairs are ignored so existing likes remain intact. Then drop the bookmark table.

Create `PLACE_LIKE` with `(place_id, user_id)` as its primary key, a `created_at` timestamp, and foreign keys to `PLACE` and `APP_USER`.

## API

Community post likes become explicit idempotent operations:

- `POST /api/community/posts/{postId}/like`
- `DELETE /api/community/posts/{postId}/like`
- `GET /api/users/me/liked-community-posts?page=&size=`

Place likes use the same contract:

- `POST /api/places/{placeId}/like`
- `DELETE /api/places/{placeId}/like`
- `GET /api/users/me/liked-places?page=&size=`

## Responses

Community responses retain `likeCount` and `likedByMe` and remove `bookmarkedByMe`.

Place summaries gain `likedByMe`. Public requests return `false`; authenticated requests resolve it from `PLACE_LIKE`. My liked-place results always return `true`.

## Frontend

- Remove bookmark API functions, state, buttons, icons, and response fields.
- Community list cards use a heart button connected to the post-like API.
- Community detail keeps its existing heart control but uses explicit add/remove APIs.
- Explore and place detail heart controls use persistent place-like APIs.
- My Page replaces "찜한 게시글" with one "좋아요" tab containing "게시글" and "여행지" sub-tabs.
- Users can open an item or remove its like from either list.

## Error Handling

Like additions and removals are idempotent. UI changes optimistically and rolls back on failure. Unauthenticated users are routed to login with the existing notification behavior.

## Verification

- Migration SQL copies bookmark data before dropping the table.
- Backend tests cover idempotent community and place likes and both My Page queries.
- Frontend API tests cover POST/DELETE and liked-content queries.
- Page tests cover optimistic state, rollback, My Page sub-tabs, and navigation.
