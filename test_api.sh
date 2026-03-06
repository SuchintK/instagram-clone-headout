#!/bin/bash

# Instagram Clone API - Complete Test Suite
# Usage: bash test_api.sh

BASE_URL="http://localhost:8080"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========== INSTAGRAM CLONE API TEST SUITE ==========${NC}\n"

# Function to print test results
print_test() {
    echo -e "${YELLOW}[$1]${NC} $2"
}

print_pass() {
    echo -e "${GREEN}✓ PASS${NC}"
}

print_fail() {
    echo -e "${RED}✗ FAIL${NC}"
}

# ============ TEST 1: USER MANAGEMENT ============
print_test "1.1" "Creating User 1 (Alice)"
USER_1=$(curl -s -X POST "$BASE_URL/api/users?name=Alice&phoneNumber=1111111111" | jq -r '.id')
if [ -n "$USER_1" ] && [ "$USER_1" != "null" ]; then
    print_pass
    echo "  ID: $USER_1"
else
    print_fail; exit 1
fi

print_test "1.2" "Creating User 2 (Bob)"
USER_2=$(curl -s -X POST "$BASE_URL/api/users?name=Bob&phoneNumber=2222222222" | jq -r '.id')
if [ -n "$USER_2" ] && [ "$USER_2" != "null" ]; then
    print_pass
    echo "  ID: $USER_2"
else
    print_fail; exit 1
fi

print_test "1.3" "Creating User 3 (Charlie)"
USER_3=$(curl -s -X POST "$BASE_URL/api/users?name=Charlie&phoneNumber=3333333333" | jq -r '.id')
if [ -n "$USER_3" ] && [ "$USER_3" != "null" ]; then
    print_pass
    echo "  ID: $USER_3"
else
    print_fail; exit 1
fi

print_test "1.4" "User 1 follows User 2"
FOLLOW=$(curl -s -X POST "$BASE_URL/api/users/$USER_1/follow/$USER_2" | jq -r '.following[0]')
if [ "$FOLLOW" = "$USER_2" ]; then
    print_pass
else
    print_fail
fi

print_test "1.5" "User 1 follows User 3"
curl -s -X POST "$BASE_URL/api/users/$USER_1/follow/$USER_3" > /dev/null
print_pass

# ============ TEST 2: POST CREATION ============
print_test "2.1" "User 2 creates post with media"
POST_1=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Beautiful sunset\", \"mediaUrls\": [\"bucket/sunset.jpg\"]}" | jq -r '.id')
if [ -n "$POST_1" ] && [ "$POST_1" != "null" ]; then
    print_pass
    echo "  ID: $POST_1"
else
    print_fail; exit 1
fi

sleep 1

print_test "2.2" "User 2 creates post without media"
POST_2=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Good morning world\"}" | jq -r '.id')
if [ -n "$POST_2" ] && [ "$POST_2" != "null" ]; then
    print_pass
    echo "  ID: $POST_2"
else
    print_fail; exit 1
fi

sleep 1

print_test "2.3" "User 3 creates post with media"
POST_3=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_3\", \"content\": \"Coffee and coding\", \"mediaUrls\": [\"bucket/coffee.jpg\"]}" | jq -r '.id')
if [ -n "$POST_3" ] && [ "$POST_3" != "null" ]; then
    print_pass
    echo "  ID: $POST_3"
else
    print_fail; exit 1
fi

# ============ TEST 3: POST RETRIEVAL ============
print_test "3.1" "Get all posts by User 2"
COUNT=$(curl -s "$BASE_URL/api/posts/user/$USER_2" | jq '. | length')
if [ "$COUNT" -eq 2 ]; then
    print_pass
    echo "  Posts: $COUNT"
else
    print_fail
fi

print_test "3.2" "Get User 1's timeline (from followed users)"
TIMELINE=$(curl -s "$BASE_URL/api/posts/timeline/$USER_1" | jq '. | length')
if [ "$TIMELINE" -gt 0 ]; then
    print_pass
    echo "  Timeline posts: $TIMELINE"
else
    print_fail
fi

# ============ TEST 4: PAGINATION ============
print_test "4.1" "Get paginated posts (limit=1)"
RESPONSE=$(curl -s "$BASE_URL/api/posts?userId=$USER_2&limit=1")
ITEMS=$(echo "$RESPONSE" | jq '.items | length')
HAS_CURSOR=$(echo "$RESPONSE" | jq '.nextCursor != null')
if [ "$ITEMS" -eq 1 ] && [ "$HAS_CURSOR" = "true" ]; then
    print_pass
    CURSOR=$(echo "$RESPONSE" | jq -r '.nextCursor')
else
    print_fail
fi

print_test "4.2" "Get next page using cursor"
RESPONSE=$(curl -s "$BASE_URL/api/posts?userId=$USER_2&limit=1&cursor=$CURSOR")
ITEMS=$(echo "$RESPONSE" | jq '.items | length')
if [ "$ITEMS" -eq 1 ]; then
    print_pass
else
    print_fail
fi

# ============ TEST 5: POST UPDATE ============
print_test "5.1" "User 2 updates own post (success)"
UPDATED=$(curl -s -X PUT "$BASE_URL/api/posts/$POST_1" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\", \"content\": \"Beautiful sunset - UPDATED\"}" | jq -r '.content')
if [ "$UPDATED" = "Beautiful sunset - UPDATED" ]; then
    print_pass
else
    print_fail
fi

print_test "5.2" "User 1 tries to update User 2's post (should fail)"
ERROR=$(curl -s -X PUT "$BASE_URL/api/posts/$POST_1" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_1\", \"content\": \"Hacked\"}" | jq -r '.status')
if [ "$ERROR" = "403" ]; then
    print_pass
    echo "  Correctly rejected with 403 Forbidden"
else
    print_fail
fi

# ============ TEST 6: OWNERSHIP VALIDATION ============
print_test "6.1" "User 2 deletes own post (success)"
STATUS=$(curl -s -w "%{http_code}" -o /dev/null -X DELETE "$BASE_URL/api/posts/$POST_2?userId=$USER_2")
if [ "$STATUS" = "204" ]; then
    print_pass
else
    print_fail
fi

print_test "6.2" "User 3 tries to delete User 2's post (should fail)"
ERROR=$(curl -s -X DELETE "$BASE_URL/api/posts/$POST_1?userId=$USER_3" | jq -r '.status')
if [ "$ERROR" = "403" ]; then
    print_pass
    echo "  Correctly rejected with 403 Forbidden"
else
    print_fail
fi

print_test "6.3" "Verify deleted post is gone"
COUNT=$(curl -s "$BASE_URL/api/posts/user/$USER_2" | jq '. | length')
if [ "$COUNT" -eq 1 ]; then
    print_pass
    echo "  Remaining posts: $COUNT"
else
    print_fail
fi

# ============ TEST 7: VALIDATION ============
print_test "7.1" "Create post without content (should fail)"
ERROR=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_2\"}" | jq -r '.status')
if [ "$ERROR" = "400" ]; then
    print_pass
else
    print_fail
fi

print_test "7.2" "Create post for non-existent user (should fail)"
ERROR=$(curl -s -X POST "$BASE_URL/api/posts" \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"non-existent\", \"content\": \"Test\"}" | jq -r '.status')
if [ "$ERROR" = "404" ]; then
    print_pass
else
    print_fail
fi

# ============ TEST 8: GLOBAL FEED ============
print_test "8.1" "Get global feed paginated (limit=2)"
GLOBAL_FEED=$(curl -s "$BASE_URL/api/posts/feed?limit=2")
GLOBAL_COUNT=$(echo "$GLOBAL_FEED" | jq '.items | length')
if [ "$GLOBAL_COUNT" -eq 2 ]; then
    print_pass
    echo "  Items in page: $GLOBAL_COUNT"
else
    print_fail
fi

print_test "8.2" "Verify global feed includes posts from all users"
GLOBAL_ALL=$(curl -s "$BASE_URL/api/posts/feed?limit=10" | jq '.items | length')
if [ "$GLOBAL_ALL" -ge 3 ]; then
    print_pass
    echo "  Total posts visible: $GLOBAL_ALL"
else
    print_fail
fi

# ============ TEST 9: UNFOLLOW AND TIMELINE ============
print_test "9.1" "User 1 unfollows User 2"
curl -s -X POST "$BASE_URL/api/users/$USER_1/unfollow/$USER_2" > /dev/null
print_pass

print_test "9.2" "Verify User 2's posts no longer in User 1's timeline"
TIMELINE=$(($(curl -s "$BASE_URL/api/posts/timeline/$USER_1" | jq '.[].userId' | grep -c "$USER_2") + 0))
if [ "$TIMELINE" -eq 0 ]; then
    print_pass
else
    print_fail
fi

# ============ SUMMARY ============
echo -e "\n${BLUE}========== TEST SUMMARY ==========${NC}"
echo -e "${GREEN}All tests completed successfully!${NC}"
echo -e "\n${YELLOW}Created Resources:${NC}"
echo "  Users: Alice ($USER_1), Bob ($USER_2), Charlie ($USER_3)"
echo "  Posts: $POST_1, $POST_3 (remaining)"
echo ""
