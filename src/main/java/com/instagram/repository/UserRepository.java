package com.instagram.repository;

import com.instagram.entity.User;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final ConcurrentHashMap<String, User> store = new ConcurrentHashMap<>();

    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(String id) {
        store.remove(id);
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}
