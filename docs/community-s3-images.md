# Community S3 Image Upload

Community image uploads are stored in Amazon S3, and the returned public image URL should be saved in MySQL through the existing `image_url` field.

## Required Environment Variables

Set these before running the backend:

```powershell
$env:AWS_ACCESS_KEY_ID="your_access_key"
$env:AWS_SECRET_ACCESS_KEY="your_secret_key"
$env:AWS_REGION="ap-northeast-2"
$env:COMMUNITY_S3_BUCKET="ssafyslapbucket"
```

Optional, when using CloudFront or a custom public domain:

```powershell
$env:COMMUNITY_S3_PUBLIC_BASE_URL="https://your-public-domain.example.com"
```

If `COMMUNITY_S3_PUBLIC_BASE_URL` is not set, image URLs are returned as:

```text
https://ssafyslapbucket.s3.ap-northeast-2.amazonaws.com/community/{uuid}.{ext}
```

## Limits

- Maximum image size: 5MB
- Maximum multipart request size: 6MB (includes multipart encoding overhead)
- Supported content types: `image/jpeg`, `image/png`, `image/webp`, `image/gif`
- S3 object key prefix: `community/`

## API Flow

1. Upload the image with `POST /api/community/images` using multipart form field `image`.
2. The API uploads the file to S3 and returns `{ "imageUrl": "..." }`.
3. Send that `imageUrl` in `POST /api/community/posts` or `PUT /api/community/posts/{postId}`.
4. MySQL stores the URL in `community_post.image_url`.
