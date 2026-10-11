package org.example.service;

import org.example.exception.NotFoundException;
import org.example.model.Chat;
import org.example.model.GroupChat;
import org.example.model.Message;
import org.example.model.NamedChat;
import org.example.model.PrivateChat;
import org.example.model.RoomChat;
import org.example.model.User;
import org.example.repository.ChatRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public PrivateChat createPrivateChat(UUID firstUserId, UUID secondUserId) throws IllegalAccessException {
        if (firstUserId.equals(secondUserId)) {
            throw new IllegalArgumentException("Для приватного чата выберите двух разных пользователей");
        }

        User firstUser = getUser(firstUserId);
        User secondUser = getUser(secondUserId);
        PrivateChat chat = new PrivateChat(idSequence.getAndIncrement(), firstUser, secondUser);
        return (PrivateChat) chatRepository.create(chat);
    }

    public GroupChat createGroupChat(String name, int maxUsers, List<UUID> userIds) {
        if (userIds == null) {
            throw new IllegalArgumentException("Список пользователей не задан");
        }

        Set<UUID> uniqueIds = new HashSet<>(userIds);
        if (uniqueIds.size() != userIds.size()) {
            throw new IllegalArgumentException("Один пользователь не может быть добавлен в групповой чат несколько раз");
        }

        List<User> users = new ArrayList<>();
        for (UUID userId : userIds) {
            users.add(getUser(userId));
        }

        GroupChat chat = new GroupChat(idSequence.getAndIncrement(), name, maxUsers, users);
        return (GroupChat) chatRepository.create(chat);
    }

    public RoomChat createRoom(String name) {
        RoomChat room = new RoomChat(idSequence.getAndIncrement(), name);
        return (RoomChat) chatRepository.create(room);
    }

    public Chat getChat(int chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Чат не найден"));
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    public void renameChat(int chatId, String newName) {
        Chat chat = getChat(chatId);
        if (!(chat instanceof NamedChat namedChat)) {
            throw new IllegalArgumentException("Приватный чат не имеет отдельного имени и не переименовывается");
        }

        namedChat.setName(newName);
        chatRepository.update(chat);
    }

    public void deleteChat(int chatId) {
        if (!chatRepository.deleteById(chatId)) {
            throw new NotFoundException("Чат не найден");
        }
    }

    public void addUserToChat(int chatId, UUID userId) {
        Chat chat = getChat(chatId);
        User user = getUser(userId);
        chat.addUser(user);
        chatRepository.update(chat);
    }

    public void removeUserFromChat(int chatId, UUID userId) {
        Chat chat = getChat(chatId);
        chat.removeUser(userId);
        chatRepository.update(chat);
    }

    public Message sendMessage(int chatId, UUID senderId, String text) {
        Chat chat = getChat(chatId);

        if (!chat.containsUser(senderId)) {
            throw new IllegalArgumentException("Пользователь не состоит в этом чате");
        }
        if (chat.isBanned(senderId)) {
            throw new IllegalStateException("Пользователю запрещено отправлять сообщения в этом чате");
        }

        User sender = getUser(senderId);
        Message message = new Message(UUID.randomUUID(), sender, text, LocalDateTime.now());
        chat.addMessage(message);
        chatRepository.update(chat);
        return message;
    }

    public List<Message> getMessages(int chatId) {
        return getChat(chatId).getMessages();
    }

    public void banUser(int chatId, UUID userId) {
        Chat chat = getChat(chatId);
        getUser(userId);
        chat.banUser(userId);
        chatRepository.update(chat);
    }

    public void unbanUser(int chatId, UUID userId) {
        Chat chat = getChat(chatId);
        getUser(userId);
        chat.unbanUser(userId);
        chatRepository.update(chat);
    }

    public List<User> getBannedUsers(int chatId) {
        Chat chat = getChat(chatId);
        return chat.getUsers().stream()
                .filter(user -> chat.isBanned(user.getId()))
                .toList();
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
