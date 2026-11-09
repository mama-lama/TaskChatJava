package org.example;

import org.example.controller.ConsoleController;
import org.example.database.Database;
import org.example.repository.ChatRepository;
import org.example.repository.UserRepository;
import org.example.repository.impl.JdbcChatRepository;
import org.example.repository.impl.JdbcUserRepository;
import org.example.service.ChatService;
import org.example.service.UserService;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Database.initialize();
            System.out.println("Подключение к PostgreSQL успешно!");
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к PostgreSQL: " + e.getMessage());
            System.out.println("Проверьте настройки в Database.java и существование базы task_chat.");
            return;
        }

        UserRepository userRepository = new JdbcUserRepository();
        ChatRepository chatRepository = new JdbcChatRepository();

        UserService userService = new UserService(userRepository, chatRepository);
        ChatService chatService = new ChatService(chatRepository, userRepository);

        ConsoleController consoleController = new ConsoleController(userService, chatService);
        consoleController.run();
    }
}
