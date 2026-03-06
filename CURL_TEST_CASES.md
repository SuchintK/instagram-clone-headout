# Instagram Clone API - cURL Test Cases

## Base URL
```
http://localhost:8080
```

## 1. USER MANAGEMENT

### Create User 1
```bash
curl -X POST "http://localhost:8080/api/users?name=Alice&phoneNumber=1234567890"
```
**Response**: User object with ID (save this ID as `USER_1`)

### Create User 2
```bash
curl -X POST "http://localhost:8080/api/users?name=Bob&phoneNumber=0987654321"
```
**Response**: User object with ID (save this ID as `USER_2`)

### Create User 3
```bash
curl -X POST "http://localhost:8080/api/users?name=Charlie&phoneNumber=1122334455"
```
**Response**: User object with ID (save this ID as `USER_3`)

### Get User Details
```bash
curl -X GET "http://localhost:8080/api/users/{USER_1}"
```

### User 1 Follows User 2
```bash
curl -X POST "http://localhost:8080/api/users/{USER_1}/follow/{USER_2}"
```

### User 1 Follows User 3
```bash
curl -X POST "http://localhost:8080/api/users/{USER_1}/follow/{USER_3}"
```

### User 1 Unfollow User 2
```bash
curl -X POST "http://localhost:8080/api/users/{USER_1}/unfollow/{USER_2}"
```

---

## 2. POST CREATION

### Create Post by User 2 (with media)
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Beautiful sunset today",
    "mediaUrls": ["bucket/sunset.jpg", "bucket/sunset2.jpg"]
  }'
```
**Response**: Post object with ID (save as `POST_1`)

### Create Post by User 2 (without media)
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Just woke up, feeling great"
  }'
```
**Response**: Post object with ID (save as `POST_2`)

### Create Post by User 3 (with media)
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_3}",
    "content": "Coffee and code",
    "mediaUrls": ["bucket/coffee.jpg"]
  }'
```
**Response**: Post object with ID (save as `POST_3`)

### Create Multiple Posts for Pagination Testing
```bash
# Post 4
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Post 4 content"
  }'

# Post 5
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Post 5 content"
  }'

# Post 6
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Post 6 content"
  }'
```

---

## 3. POST RETRIEVAL

### Get All Posts by User 2
```bash
curl -X GET "http://localhost:8080/api/posts/user/{USER_2}"
```
**Expected**: List of all posts by User 2, sorted by recency

### Get Posts with Pagination (limit=2)
```bash
curl -X GET "http://localhost:8080/api/posts?userId={USER_2}&limit=2"
```
**Expected**: First 2 posts with `nextCursor` for pagination

### Get Next Page of Posts using Cursor
```bash
curl -X GET "http://localhost:8080/api/posts?userId={USER_2}&limit=2&cursor={nextCursor_from_previous}"
```
**Expected**: Next page of posts

### Get Timeline for User 1 (posts from followed users)
```bash
curl -X GET "http://localhost:8080/api/posts/timeline/{USER_1}"
```
**Expected**: Posts from User 2 and User 3 (whom User 1 follows), sorted by recency

### Get Global Feed (all posts from all users, paginated)
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=5"
```
**Expected**: First 5 posts from all users, sorted by recency (most recent first)

### Get Next Page of Global Feed using Cursor
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=5&cursor={nextCursor}"
```
**Expected**: Next 5 posts using cursor pagination

---

## 4. POST UPDATE

### Update Own Post (Success)
```bash
curl -X PUT "http://localhost:8080/api/posts/{POST_1}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Updated sunset photo - even more beautiful"
  }'
```
**Expected**: 200 OK with updated post

### Try to Update Someone Else's Post (Forbidden)
```bash
curl -X PUT "http://localhost:8080/api/posts/{POST_1}" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_1}",
    "content": "Hacker trying to change content"
  }'
```
**Expected**: 403 FORBIDDEN - "You can only update your own posts"

### Update Non-existent Post (Not Found)
```bash
curl -X PUT "http://localhost:8080/api/posts/non-existent-id" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}",
    "content": "Some content"
  }'
```
**Expected**: 404 NOT_FOUND - "Post not found"

---

## 5. POST DELETION

### Delete Own Post (Success - Soft Delete)
```bash
curl -X DELETE "http://localhost:8080/api/posts/{POST_2}?userId={USER_2}"
```
**Expected**: 204 NO_CONTENT (post marked as inactive)

### Try to Delete Someone Else's Post (Forbidden)
```bash
curl -X DELETE "http://localhost:8080/api/posts/{POST_1}?userId={USER_3}"
```
**Expected**: 403 FORBIDDEN - "You can only delete your own posts"

### Try to Delete Non-existent Post (Not Found)
```bash
curl -X DELETE "http://localhost:8080/api/posts/non-existent-id?userId={USER_1}"
```
**Expected**: 404 NOT_FOUND - "Post not found"

### Verify Deleted Post is Gone from User's Posts
```bash
curl -X GET "http://localhost:8080/api/posts/user/{USER_2}"
```
**Expected**: Deleted post should not appear in the list

---

## 6. PAGINATION TESTING

### User Posts Pagination - Get First Page (limit=1)
```bash
curl -X GET "http://localhost:8080/api/posts?userId={USER_2}&limit=1"
```
**Save `nextCursor`**

### User Posts Pagination - Get Second Page Using Cursor
```bash
curl -X GET "http://localhost:8080/api/posts?userId={USER_2}&limit=1&cursor={nextCursor_1}"
```
**Save new `nextCursor`**

### User Posts Pagination - Get Third Page Using Cursor
```bash
curl -X GET "http://localhost:8080/api/posts?userId={USER_2}&limit=1&cursor={nextCursor_2}"
```
**Expected**: When no more items, `nextCursor` should be `null` and `hasMore` should be `false`

### Global Feed Pagination - Get First Page (limit=3)
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=3"
```
**Expected**: First 3 most recent posts from ALL users
**Save `nextCursor`**

### Global Feed Pagination - Get Next Page
```bash
curl -X GET "http://localhost:8080/api/posts/feed?limit=3&cursor={nextCursor}"
```
**Expected**: Next 3 posts using cursor-based pagination

---

## 7. VALIDATION TESTING

### Create Post without Required Field (missing content)
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{USER_2}"
  }'
```
**Expected**: 400 BAD_REQUEST - "content is required"

### Create Post without userId
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Some content"
  }'
```
**Expected**: 400 BAD_REQUEST - "userId is required"

### Create Post for Non-existent User
```bash
curl -X POST "http://localhost:8080/api/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "non-existent-user-id",
    "content": "Some content"
  }'
```
**Expected**: 404 NOT_FOUND - "User not found"

---

## 8. TIMELINE TESTING

### User 1's Timeline (should include posts from User 2 and User 3)
```bash
curl -X GET "http://localhost:8080/api/posts/timeline/{USER_1}"
```
**Expected**:
- Posts from User 2 (whom User 1 follows)
- Posts from User 3 (whom User 1 follows)
- Sorted by `createdAt` DESC (newest first)

### Unfollow User 2, Then Check Timeline
```bash
# Step 1: Unfollow
curl -X POST "http://localhost:8080/api/users/{USER_1}/unfollow/{USER_2}"

# Step 2: Check timeline
curl -X GET "http://localhost:8080/api/posts/timeline/{USER_1}"
```
**Expected**: Posts from User 2 should no longer appear

---

## 9. COMPLETE WORKFLOW TEST SCRIPT

Run this bash script to test the complete flow:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "========== INSTAGRAM CLONE TEST SUITE =========="

# 1. Create Users
echo -e "\n[1] Creating Users..."
USER_1=$(curl -s -X POST "$BASE_URL/api/users?name=Alice&phoneNumber=1111111111" | jq -r '.id')
USER_2=$(curl -s -X POST "$BASE_URL/api/users?name=Bob&phoneNumber=2222222222" | jq -r '.id')
USER_3=$(curl -s -X POST "$BASE_URL/api/users?name=Charlie&phoneNumber=3333333333" | jq -r '.id')

echo "User 1 (Alice): $USER_1"
echo "User 2 (Bob): $USER_2"
echo "User 3 (Charlie): $USER_3"

# 2. Follow Users
echo -e "\n[2] Setting up follow relationships..."
curl -s -X POST "$BASE_URL/api/users/$USER_1/follow/$USER_2" > /dev/null
curl -s -X POST "$BASE_URL/api/users/$USER_1/follow/$USER_3" > /dev/null
echo "User 1 now follows User 2 and User 3"

# 3. Create Posts
echo -e "\n[3] Creating posts..."
POST_1=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Bob's first post\", \"mediaUrls\": [\"bucket/bob1.jpg\"]}" | jq -r '.id')
echo "Post 1 by Bob: $POST_1"

sleep 1

POST_2=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Bob's second post\"}" | jq -r '.id')
echo "Post 2 by Bob: $POST_2"

sleep 1

POST_3=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_3\", \"content\": \"Charlie's first post\", \"mediaUrls\": [\"bucket/charlie1.jpg\"]}" | jq -r '.id')
echo "Post 3 by Charlie: $POST_3"

# 4. Get User's Posts
echo -e "\n[4] Getting all posts by Bob..."
curl -s "$BASE_URL/api/posts/user/$USER_2" | jq '.[] | {id, content, userId}'

# 5. Get Timeline
echo -e "\n[5] Getting Alice's timeline (posts from followed users)..."
curl -s "$BASE_URL/api/posts/timeline/$USER_1" | jq '.[] | {id, content, userId, createdAt}'

# 6. Pagination Test
echo -e "\n[6] Testing pagination (limit=1)..."
PAGE_1=$(curl -s "$BASE_URL/api/posts?userId=$USER_2&limit=1")
echo "Page 1:"
echo "$PAGE_1" | jq '{items: .items[].content, hasMore, nextCursor}'

CURSOR=$(echo "$PAGE_1" | jq -r '.nextCursor')
if [ "$CURSOR" != "null" ]; then
  PAGE_2=$(curl -s "$BASE_URL/api/posts?userId=$USER_2&limit=1&cursor=$CURSOR")
  echo "Page 2:"
  echo "$PAGE_2" | jq '{items: .items[].content, hasMore, nextCursor}'
fi

# 7. Update Post
echo -e "\n[7] Updating Bob's first post..."
curl -s -X PUT "$BASE_URL/api/posts/$POST_1" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Bob's first post - UPDATED\"}" | jq '.content'

# 8. Test Ownership (Should Fail)
echo -e "\n[8] Testing ownership - Alice tries to update Bob's post (should fail)..."
curl -s -X PUT "$BASE_URL/api/posts/$POST_1" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_1\", \"content\": \"Hacked\"}" | jq '{error: .error, message: .message}'

# 9. Delete Post
echo -e "\n[9] Bob deletes his second post..."
curl -s -X DELETE "$BASE_URL/api/posts/$POST_2?userId=$USER_2" -w "\nStatus: %{http_code}\n"

# 10. Verify Deletion
echo -e "\n[10] Verifying deletion - Bob's remaining posts..."
curl -s "$BASE_URL/api/posts/user/$USER_2" | jq '.[] | {id, content}'

echo -e "\n========== TEST COMPLETE =========="
```

Save this as `test_instagram_api.sh` and run with:
```bash
chmod +x test_instagram_api.sh
./test_instagram_api.sh
```

---

## Expected Responses Format

### Success Response (Create Post)
```json
{
  "id": "uuid-here",
  "content": "Post content",
  "userId": "user-id",
  "createdAt": 1772789005243,
  "updatedAt": 1772789005243,
  "media": [
    {
      "id": "media-uuid",
      "postId": "post-uuid",
      "s3URL": "bucket/filename.jpg",
      "createdAt": 1772789005243,
      "updatedAt": 1772789005243
    }
  ]
}
```

### Paginated Response
```json
{
  "items": [
    { "id": "...", "content": "..." }
  ],
  "nextCursor": "post-id-or-null",
  "hasMore": true
}
```

### Error Response (403 Forbidden)
```json
{
  "error": "403 FORBIDDEN",
  "message": "You can only update your own posts",
  "timestamp": "2026-03-06T14:55:10.890033",
  "status": 403
}
```

---

## Tips for Testing

1. **Save IDs**: When creating users/posts, save their IDs for use in subsequent requests
2. **Use jq**: Pipe responses to `jq` for pretty-printed JSON output
3. **Timing**: Add `sleep 1` between post creations for different timestamps (tests recency sorting)
4. **Verify with GET**: After POST/PUT/DELETE, verify with a GET request
5. **Check Status Codes**: Use `-w "\nStatus: %{http_code}\n"` to see HTTP status
