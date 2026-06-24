# Community Multi-Image Upload Fix

## Goal

Allow community posts containing more than five total text/image cells to be saved, while enforcing a 5MB per-image upload limit consistently.

## Root Cause

The frontend editor intentionally allows more than five cells, but `CommunityService` rejects requests whose `cells` list contains more than five entries. Images are uploaded before the post request, so the uploads can succeed while the final post save returns HTTP 400.

The original frontend accepted images up to 5MB while Spring multipart handling and `CommunityImageStorageService` rejected files larger than 2MB.

## Design

- Remove the backend's five-cell validation and its obsolete rejection test.
- Replace that test with a regression test proving that a post containing six cells is normalized and persisted.
- Raise the backend multipart and storage limits to 5MB.
- Keep frontend file validation and its visible helper text at 5MB.
- Add regression tests proving that files between 2MB and 5MB are accepted and files over 5MB are rejected.

## Data Flow

Each selected image continues to be uploaded independently through `POST /api/community/images`. The returned URLs are collected into the ordered `cells` payload, which is then sent through `POST /api/community/posts` or `PUT /api/community/posts/{postId}` without an application-level cell-count limit.

## Error Handling

Images larger than 5MB are rejected immediately by the editor with the existing inline error area. The backend remains the final enforcement layer through both Spring multipart configuration and `CommunityImageStorageService`.

## Verification

- Backend service test: six cells are accepted and passed to `insertPostCells`.
- Backend image storage tests: images up to 5MB are accepted and larger images are rejected.
- Frontend editor tests: multiple cells remain allowed, 3MB images are accepted, and images over 5MB are rejected.
- Run focused frontend and backend community test suites.
