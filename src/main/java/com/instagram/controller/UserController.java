package com.instagram.controller;

import com.instagram.entity.User;
import com.instagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String name, @RequestParam(required = false) String phoneNumber) {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, name, phoneNumber);
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userRepository.findById(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/follow/{followId}")
    public ResponseEntity<User> followUser(@PathVariable String userId, @PathVariable String followId) {
        User user = userRepository.findById(userId).orElse(null);
        User followUser = userRepository.findById(followId).orElse(null);

        if (user == null || followUser == null) {
            return ResponseEntity.notFound().build();
        }

        user.getFollowing().add(followId);
        followUser.getFollowers().add(userId);

        userRepository.save(user);
        userRepository.save(followUser);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/unfollow/{followId}")
    public ResponseEntity<User> unfollowUser(@PathVariable String userId, @PathVariable String followId) {
        User user = userRepository.findById(userId).orElse(null);
        User followUser = userRepository.findById(followId).orElse(null);

        if (user == null || followUser == null) {
            return ResponseEntity.notFound().build();
        }

        user.getFollowing().remove(followId);
        followUser.getFollowers().remove(userId);

        userRepository.save(user);
        userRepository.save(followUser);

        return ResponseEntity.ok(user);
    }
}
