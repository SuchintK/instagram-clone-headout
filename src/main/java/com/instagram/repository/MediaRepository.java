package com.instagram.repository;

import com.instagram.entity.Media;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MediaRepository {
    private final ConcurrentHashMap<String, Media> store = new ConcurrentHashMap<>();

    public Media save(Media media) {
        store.put(media.getId(), media);
        return media;
    }

    public Optional<Media> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Media> findByPostId(String postId) {
        return store.values().stream()
            .filter(m -> m.getPostId().equals(postId) && !m.isInactiveFlag())
            .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
