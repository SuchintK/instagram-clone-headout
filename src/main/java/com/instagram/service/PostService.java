package com.instagram.service;

import com.instagram.dto.request.CreatePostRequest;
import com.instagram.dto.request.UpdatePostRequest;
import com.instagram.dto.response.CursorPageResponse;
import com.instagram.dto.response.MediaResponse;
import com.instagram.dto.response.PostResponse;
import com.instagram.entity.Media;
import com.instagram.entity.Post;
import com.instagram.entity.User;
import com.instagram.repository.MediaRepository;
import com.instagram.repository.PostRepository;
import com.instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public PostResponse createPost(CreatePostRequest request) {
        String userId = request.getUserId();

        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Create post
        String postId = UUID.randomUUID().toString();
        Long now = System.currentTimeMillis();

        Post post = Post.builder()
            .id(postId)
            .content(request.getContent())
            .createdAt(now)
            .updatedAt(now)
            .userId(userId)
            .inactiveFlag(false)
            .build();

        Post savedPost = postRepository.save(post);

        // Create media if provided
        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            for (String url : request.getMediaUrls()) {
                Media media = Media.builder()
                    .id(UUID.randomUUID().toString())
                    .postId(postId)
                    .s3URL(url)
                    .createdAt(now)
                    .updatedAt(now)
                    .inactiveFlag(false)
                    .build();
                mediaRepository.save(media);
            }
        }

        return toPostResponse(savedPost);
    }

    public List<PostResponse> getPostsByUser(String userId) {
        return postRepository.findByUserId(userId).stream()
            .sorted(Comparator.comparingLong(Post::getCreatedAt).reversed())
            .map(this::toPostResponse)
            .collect(Collectors.toList());
    }

    public CursorPageResponse<PostResponse> getPostsPage(String userId, int limit, String cursor) {
        // Get all active posts for the user
        List<Post> allPosts = postRepository.findByUserId(userId).stream()
            .sorted(Comparator.comparingLong(Post::getCreatedAt).reversed()
                .thenComparing(Post::getId))
            .collect(Collectors.toList());

        // Find start index based on cursor
        int startIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            for (int i = 0; i < allPosts.size(); i++) {
                if (allPosts.get(i).getId().equals(cursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }

        // Get the page
        int endIndex = Math.min(startIndex + limit, allPosts.size());
        List<Post> page = allPosts.subList(startIndex, endIndex);

        // Determine next cursor
        String nextCursor = null;
        boolean hasMore = false;
        if (page.size() == limit && endIndex < allPosts.size()) {
            nextCursor = page.get(page.size() - 1).getId();
            hasMore = true;
        }

        List<PostResponse> items = page.stream()
            .map(this::toPostResponse)
            .collect(Collectors.toList());

        return CursorPageResponse.<PostResponse>builder()
            .items(items)
            .nextCursor(nextCursor)
            .hasMore(hasMore)
            .build();
    }

    public List<PostResponse> getTimeline(String userId) {
        // Get user's following list
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Collect posts from all followed users
        List<Post> timelinePosts = postRepository.findAll().stream()
            .filter(p -> user.getFollowing().contains(p.getUserId()) && !p.isInactiveFlag())
            .sorted(Comparator.comparingLong(Post::getCreatedAt).reversed())
            .collect(Collectors.toList());

        return timelinePosts.stream()
            .map(this::toPostResponse)
            .collect(Collectors.toList());
    }

    public CursorPageResponse<PostResponse> getGlobalFeedPaginated(int limit, String cursor) {
        // Get all active posts from all users
        List<Post> allPosts = postRepository.findAll().stream()
            .filter(p -> !p.isInactiveFlag())
            .sorted(Comparator.comparingLong(Post::getCreatedAt).reversed()
                .thenComparing(Post::getId))
            .collect(Collectors.toList());

        // Find start index based on cursor
        int startIndex = 0;
        if (cursor != null && !cursor.isEmpty()) {
            for (int i = 0; i < allPosts.size(); i++) {
                if (allPosts.get(i).getId().equals(cursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }

        // Get the page
        int endIndex = Math.min(startIndex + limit, allPosts.size());
        List<Post> page = allPosts.subList(startIndex, endIndex);

        // Determine next cursor
        String nextCursor = null;
        boolean hasMore = false;
        if (page.size() == limit && endIndex < allPosts.size()) {
            nextCursor = page.get(page.size() - 1).getId();
            hasMore = true;
        }

        List<PostResponse> items = page.stream()
            .map(this::toPostResponse)
            .collect(Collectors.toList());

        return CursorPageResponse.<PostResponse>builder()
            .items(items)
            .nextCursor(nextCursor)
            .hasMore(hasMore)
            .build();
    }

    public PostResponse updatePost(String postId, UpdatePostRequest request) {
        String userId = request.getUserId();
        String content = request.getContent();

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // Check ownership
        if (!post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own posts");
        }

        post.setContent(content);
        post.setUpdatedAt(System.currentTimeMillis());

        Post updatedPost = postRepository.save(post);
        return toPostResponse(updatedPost);
    }

    public void deletePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // Check ownership
        if (!post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }

        // Soft delete
        post.setInactiveFlag(true);
        post.setUpdatedAt(System.currentTimeMillis());
        postRepository.save(post);
    }

    private PostResponse toPostResponse(Post post) {
        List<MediaResponse> mediaResponses = mediaRepository.findByPostId(post.getId()).stream()
            .map(media -> MediaResponse.builder()
                .id(media.getId())
                .postId(media.getPostId())
                .s3URL(media.getS3URL())
                .createdAt(media.getCreatedAt())
                .updatedAt(media.getUpdatedAt())
                .build())
            .collect(Collectors.toList());

        return PostResponse.builder()
            .id(post.getId())
            .content(post.getContent())
            .userId(post.getUserId())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .media(mediaResponses)
            .build();
    }
}
