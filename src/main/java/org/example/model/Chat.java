package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class Chat {
    private final int id;
    private final List<User> users = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private final Set<UUID> bannedUserIds = new HashSet<>();

    public Chat(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public Set<UUID> getBannedUserIds() {
        return Collections.unmodifiableSet(bannedUserIds);
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (containsUser(user.getId())) {
            throw new IllegalArgumentException("Пользователь уже состоит в чате");
        }
        if (users.size() >= getMaxUsers()) {
            throw new IllegalStateException("В чате достигнут лимит пользователей");
        }
        users.add(user);
    }
    public void removeUser(UUID userId) {
        if (!containsUser(userId)) {
            throw new IllegalArgumentException("Пользователь не состоит в чате");
        }
        if (users.size() - 1 < getMinUsers()) {
            throw new IllegalStateException(
                    "Нельзя удалить пользователя: для этого типа чата нужно минимум " + getMinUsers() + " участника(ов)"
            );
        }

        users.removeIf(user -> user.getId().equals(userId));
        bannedUserIds.remove(userId);
    }

    public boolean containsUser(UUID userId) {
        return users.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void banUser(UUID userId) {
        if (!containsUser(userId)) {
            throw new IllegalArgumentException("Нельзя забанить пользователя, которого нет в чате");
        }
        if (!bannedUserIds.add(userId)) {
            throw new IllegalStateException("Пользователь уже забанен в этом чате");
        }
    }

    public void unbanUser(UUID userId) {
        if (!bannedUserIds.remove(userId)) {
            throw new IllegalStateException("Пользователь не забанен в этом чате");
        }
    }

    public boolean isBanned(UUID userId) {
        return bannedUserIds.contains(userId);
    }

    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Сообщение не может быть null");
        }
        messages.add(message);
    }

    public abstract int getMinUsers();

    public abstract int getMaxUsers();

    public abstract String getName();

    public abstract String getType();
}
