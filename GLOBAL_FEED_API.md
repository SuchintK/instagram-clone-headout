# Global Feed API - Complete Guide

## Overview
The Global Feed API retrieves **all posts from all users** in the system, sorted by **recency (most recent first)**, with **cursor-based pagination**.

## Endpoint

### Get Global Feed (Paginated)
```
GET /api/posts/feed?limit={limit}&cursor={cursor}
```

## Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | integer | No | 10 | Number of posts per page (max recommended: 50) |
| `cursor` | string | No | null | Post ID of the last item from previous page for pagination |

## Response Format

```json
{
  "items": [
    {
      "id": "uuid-string",
      "content": "Post content",
      "userId": "user-id",
      "createdAt": 1772789830229,
      "updatedAt": 1772789830229,
      "media": [
        {
          "id": "media-uuid",
          "postId": "post-uuid",
          "s3URL": "bucket/filename.jpg",
          "createdAt": 1772789830229,
          "updatedAt": 1772789830229
        }
      ]
    }
  ],
  "nextCursor": "post-id-for-next-page",
  "hasMore": true
}
```

## Usage Examples

### Example 1: Get First Page of Feed
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=5"
```

**Response (first 5 most recent posts from all users):**
```json
{
  "items": [
    {"id": "post-5", "content": "Latest post", "userId": "user-1", ...},
    {"id": "post-4", "content": "Fourth post", "userId": "user-2", ...},
    {"id": "post-3", "content": "Third post", "userId": "user-3", ...},
    {"id": "post-2", "content": "Second post", "userId": "user-1", ...},
    {"id": "post-1", "content": "First post", "userId": "user-2", ...}
  ],
  "nextCursor": "post-1",
  "hasMore": true
}
```

### Example 2: Get Next Page Using Cursor
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=5&cursor=post-1"
```

**Response (next 5 posts):**
```json
{
  "items": [
    {"id": "post-0", "content": "Older post", "userId": "user-3", ...}
  ],
  "nextCursor": null,
  "hasMore": false
}
```

### Example 3: Get Feed with Default Limit (10)
```bash
curl -X GET "http://localhost:8080/api/posts/feed"
```

## Key Differences from Other Endpoints

| Endpoint | Scope | Pagination | Sorting |
|----------|-------|-----------|---------|
| `GET /api/posts?userId={id}` | Single user's posts | ✓ Cursor-based | By createdAt DESC |
| `GET /api/posts/timeline/{userId}` | Followed users' posts | ✗ No pagination | By createdAt DESC |
| `GET /api/posts/feed` | **ALL users' posts** | ✓ Cursor-based | By createdAt DESC |

## Pagination Details

### How Pagination Works

1. **First Request**: Omit `cursor` parameter
   ```bash
   curl "http://localhost:8080/api/posts/feed?limit=10"
   ```
   Returns: First 10 posts + `nextCursor`

2. **Next Request**: Use the `nextCursor` value from previous response
   ```bash
   curl "http://localhost:8080/api/posts/feed?limit=10&cursor={nextCursor}"
   ```
   Returns: Next 10 posts + new `nextCursor`

3. **Stop Condition**: When `hasMore` is `false` or `nextCursor` is `null`
   - No more posts available
   - You've reached the end of the feed

### Cursor Stability

- Cursor = Last post's ID from previous page
- Based on `createdAt` timestamp (not insertion order)
- Stable across multiple requests
- Safe for paginating through large datasets

## Real-World Use Cases

### 1. Mobile App - Infinite Scroll Feed
```bash
# Initial load
curl "http://localhost:8080/api/posts/feed?limit=20"

# When user scrolls to bottom
# Use nextCursor from previous response
curl "http://localhost:8080/api/posts/feed?limit=20&cursor={nextCursor}"
```

### 2. Web Dashboard - Page Numbers
```bash
# Page 1
curl "http://localhost:8080/api/posts/feed?limit=25"

# Page 2
curl "http://localhost:8080/api/posts/feed?limit=25&cursor={page1_nextCursor}"

# Page 3
curl "http://localhost:8080/api/posts/feed?limit=25&cursor={page2_nextCursor}"
```

### 3. API Testing
```bash
# Get all posts (small dataset)
curl "http://localhost:8080/api/posts/feed?limit=100" | jq '.items | length'

# Get posts with details
curl "http://localhost:8080/api/posts/feed?limit=5" | jq '.items[] | {id, content, userId}'

# Check pagination status
curl "http://localhost:8080/api/posts/feed?limit=10" | jq '{itemCount: (.items | length), hasMore, nextCursor}'
```

## Performance Considerations

1. **Limit Size**:
   - Small limit (5-10): Fast response, more API calls needed
   - Large limit (50+): More data per request, fewer API calls

2. **Cursor Efficiency**:
   - Cursor uses timestamp + ID indexing
   - O(N) in-memory search (efficient for small datasets)
   - No database joins needed

3. **Recommended Limits**:
   - Mobile app: 10-15 items
   - Web dashboard: 20-50 items
   - Batch processing: Up to 100 items

## Error Responses

### Invalid Cursor
```bash
curl "http://localhost:8080/api/posts/feed?limit=10&cursor=invalid-id"
```
**Response**: Returns posts from beginning (cursor treated as not found)

### Large Limit
```bash
curl "http://localhost:8080/api/posts/feed?limit=1000"
```
**Response**: Returns all available posts (no max limit enforced)

## Comparison: Timeline vs Global Feed

### Timeline Endpoint
```bash
# Get posts from users you follow (no pagination)
curl "http://localhost:8080/api/posts/timeline/{userId}"
```
- Only posts from users in your "following" list
- Not paginated
- Best for: Social feed (like Twitter/Instagram home)

### Global Feed Endpoint
```bash
# Get posts from ALL users (with pagination)
curl "http://localhost:8080/api/posts/feed?limit=10"
```
- Posts from every user
- Full cursor-based pagination
- Best for: Discover/Explore page (like Instagram Explore)

## Implementation Notes

- Uses same cursor pagination logic as user-specific posts endpoint
- Filters out soft-deleted posts (`inactiveFlag = true`)
- Sorts by `createdAt DESC` (newest first), with ID as tiebreaker
- Thread-safe (uses `ConcurrentHashMap`)
- No external database required
