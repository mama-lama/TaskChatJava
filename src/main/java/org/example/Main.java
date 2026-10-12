package org.example;

import org.example.controller.ConsoleController;
import org.example.repository.ChatRepository;
import org.example.repository.UserRepository;
import org.example.repository.impl.InMemoryChatRepository;
import org.example.repository.impl.InMemoryUserRepository;
import org.example.service.ChatService;
import org.example.service.UserService;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        ChatRepository chatRepository = new InMemoryChatRepository();

        UserService userService = new UserService(userRepository, chatRepository);
        ChatService chatService = new ChatService(chatRepository, userRepository);

        ConsoleController consoleController = new ConsoleController(userService, chatService);
        consoleController.run();
    }
}
