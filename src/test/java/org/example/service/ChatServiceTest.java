package org.example.service;

import org.example.model.Chat;
import org.example.model.PrivateChat;
import org.example.model.GroupChat;
import org.example.model.RoomChat;
import org.example.model.User;
import org.example.model.Message;
import org.example.repository.ChatRepository;
import org.example.repository.UserRepository;
import org.example.repository.impl.InMemoryUserRepository;
import org.example.repository.impl.InMemoryChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ChatServiceTest {
    private UserService userService;
    private ChatService chatService;

    @BeforeEach
    void setUp(){
        UserRepository userRepository = new InMemoryUserRepository();
        ChatRepository chatRepository = new InMemoryChatRepository();
        userService = new UserService(userRepository, chatRepository);
        chatService = new ChatService(chatRepository, userRepository);
    }

    @Test
    void privateChatContainsExactlyTwoUsers(){
        User ksu = userService.createUser("Ксения");
        User vlad = userService.createUser("Влад");
        User garri = userService.createUser("Гарик");

        PrivateChat chat = chatService.createPrivateChat(ksu.getId(), vlad.getId());

        assertEquals(2, chat.getUsers().size());
        assertThrows(IllegalStateException.class,
                () -> chatService.addUserToChat(chat.getId(), garri.getId()));
        assertThrows(IllegalStateException.class,
                () -> chatService.removeUserFromChat(chat.getId(), vlad.getId()));
    }

    @Test
    void groupChatRequiresAtLeastThreeUsersAndRespectsLimit(){
        User first = userService.createUser("Првый");
        User second = userService.createUser("Второй");
        User third = userService.createUser("Третий");
        User fourth = userService.createUser("Четвёртый");
        User fifth = userService.createUser("Пятый");

        GroupChat chat = chatService.createGroupChat(
                "Сквад",
                4,
                List.of(first.getId(), second.getId(), third.getId())
        );

        chatService.addUserToChat(chat.getId(), fourth.getId());

        assertEquals(4, chat.getUsers().size());
        assertThrows(IllegalStateException.class,
                () -> chatService.addUserToChat(chat.getId(), fifth.getId()));
    }

    @Test
    void twoUsersCanSendMessagesToOneRoom(){
        User ksu = userService.createUser("Ксения");
        User vlad = userService.createUser("Влад");
        RoomChat roomChat = chatService.createRoom("Общая комната");

        chatService.addUserToChat(roomChat.getId(), ksu.getId());
        chatService.addUserToChat(roomChat.getId(), vlad.getId());

        chatService.sendMessage(roomChat.getId(), ksu.getId(), "Привки!");
        chatService.sendMessage(roomChat.getId(), vlad.getId(), "Доброго дня!");

        assertEquals(2, chatService.getMessages(roomChat.getId()).size());
        assertEquals("Ксения", chatService.getMessages(roomChat.getId()).get(0).getSender().getName());
        assertEquals("Влад", chatService.getMessages(roomChat.getId()).get(1).getSender().getName());
    }

    @Test
    void banWorksOnlyInsideSelectedChat(){
        User ksu = userService.createUser("Ксения");
        User vlad = userService.createUser("Влад");
        RoomChat firstRoom = chatService.createRoom("Первая");
        RoomChat secondRoom = chatService.createRoom("Вторая");

        for (Chat chat : List.of(firstRoom, secondRoom)){
            chatService.addUserToChat(chat.getId(), ksu.getId());
            chatService.addUserToChat(chat.getId(), vlad.getId());
        }

        chatService.banUser(firstRoom.getId(), ksu.getId());

        assertTrue(firstRoom.isBanned(ksu.getId()));
        assertFalse(secondRoom.isBanned(ksu.getId()));
        assertThrows(IllegalStateException.class,
                () -> chatService.sendMessage(firstRoom.getId(), ksu.getId(), "Не отправляется сообщение!"));

        chatService.sendMessage(secondRoom.getId(), ksu.getId(), "А здесь сообщение отправляется!!!");
        assertEquals(1, secondRoom.getMessages().size());

    }

    @Test
    void deletingUserDoesNotLeaveInvalidChat(){
        User first = userService.createUser("Первый");
        User second = userService.createUser("Второй");
        PrivateChat privateChat = chatService.createPrivateChat(first.getId(), second.getId());

        userService.deleteUser(first.getId());

        assertThrows(RuntimeException.class,
                () -> chatService.getChat(privateChat.getId()));
    }
}
