package com.instagram.repository;

import com.instagram.entity.Post;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class PostRepository {
    private final ConcurrentHashMap<String, Post> store = new ConcurrentHashMap<>();

    public Post save(Post post) {
        store.put(post.getId(), post);
        return post;
    }

    public Optional<Post> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Post> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Post> findByUserId(String userId) {
        return store.values().stream()
            .filter(p -> p.getUserId().equals(userId) && !p.isInactiveFlag())
            .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
