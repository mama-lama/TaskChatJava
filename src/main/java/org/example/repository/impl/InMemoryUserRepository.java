package org.example.repository.impl;

import org.example.model.User;
import org.example.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UUID, User> storage = new LinkedHashMap<>();

    @Override
    public User create(User user) {
        if (storage.containsKey(user.getId())) {
            throw new IllegalArgumentException("Пользователь с таким id уже существует");
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User update(User user) {
        if (!storage.containsKey(user.getId())) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean deleteById(UUID id) {
        return storage.remove(id) != null;
    }
}
