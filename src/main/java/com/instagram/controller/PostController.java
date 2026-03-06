package com.instagram.controller;

import com.instagram.dto.request.CreatePostRequest;
import com.instagram.dto.request.UpdatePostRequest;
import com.instagram.dto.response.CursorPageResponse;
import com.instagram.dto.response.PostResponse;
import com.instagram.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<PostResponse>> getPosts(
            @RequestParam String userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String order_by,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(postService.getPostsPage(userId, limit, cursor));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getUserPosts(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    @GetMapping("/timeline/{userId}")
    public ResponseEntity<List<PostResponse>> getTimeline(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getTimeline(userId));
    }

    @GetMapping("/feed")
    public ResponseEntity<CursorPageResponse<PostResponse>> getGlobalFeed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(postService.getGlobalFeedPaginated(limit, cursor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            @RequestParam String userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
