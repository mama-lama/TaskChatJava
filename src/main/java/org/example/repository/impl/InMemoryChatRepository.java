package org.example.repository.impl;

import org.example.model.Chat;
import org.example.repository.ChatRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryChatRepository implements ChatRepository {
    private final Map<Integer, Chat> storage = new LinkedHashMap<>();

    @Override
    public Chat create(Chat chat) {
        if (storage.containsKey(chat.getId())) {
            throw new IllegalArgumentException("Чат с таким id уже существует");
        }
        storage.put(chat.getId(), chat);
        return chat;
    }

    @Override
    public Optional<Chat> findById(Integer id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Chat> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Chat update(Chat chat) {
        if (!storage.containsKey(chat.getId())) {
            throw new IllegalArgumentException("Чат не найден");
        }
        storage.put(chat.getId(), chat);
        return chat;
    }

    @Override
    public boolean deleteById(Integer id) {
        return storage.remove(id) != null;
    }
}
