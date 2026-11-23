package org.example.service;

import org.example.exception.NotFoundException;
import org.example.model.Chat;
import org.example.model.User;
import org.example.repository.ChatRepository;
import org.example.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public UserService(UserRepository userRepository, ChatRepository chatRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
    }

    public User createUser(String name) {
        User user = new User(UUID.randomUUID(), name);
        return userRepository.create(user);
    }

    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User renameUser(UUID id, String newName) {
        User user = getUser(id);
        user.setName(newName);
        return userRepository.update(user);
    }

    public void deleteUser(UUID id) {
        getUser(id);

        for (Chat chat : chatRepository.findAll()) {
            if (!chat.containsUser(id)) {
                continue;
            }

            if (chat.getUsers().size() - 1 < chat.getMinUsers()) {
                chatRepository.deleteById(chat.getId());
            } else {
                chat.removeUser(id);
                chatRepository.update(chat);
            }
        }
        userRepository.deleteById(id);
    }
}
