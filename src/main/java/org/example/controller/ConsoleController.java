package org.example.controller;

import org.example.model.User;
import org.example.service.ChatService;
import org.example.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConsoleController {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserService userService;
    private final ChatService chatService;
    private final Scanner scanner;

    public ConsoleController(UserService userService, ChatService chatService){
        this.userService = userService;
        this.chatService = chatService;
        this.scanner = new Scanner(System.in);
    }

    public void run(){
        System.out.println("=== Консольный чат ===");
        boolean running = true;

        while (running) {
            printMainMenu();
            String command = scanner.nextLine().trim();

            try{
                switch (command){
                    case "1" -> createUser();
                    case "2" -> showUsers();
                    case "3" -> renameUser();
                    case "4" -> deleteUser();
                    case "5" -> createPrivateChat();
                    case "6" -> creteGroupChat();
                    case "7" -> createRoom();
                    case "8" -> showChat();
                    case "9" -> renameChat();
                    case "10" -> deleteChat();
                    case "11" -> addUserToChat();
                    case "12" -> removeUserFromChat();
                    case "13" -> banUser();
                    case "14" -> unbanUser();
                    case "15" -> showBannedUsers();
                    case "16" -> openChat();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (RuntimeException e){
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        System.out.println("Программа завершена!");
    }

    private void printMainMenu(){
        System.out.println();
        System.out.println("--- Пользователи ---");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Показать пользователей");
        System.out.println("3. Переименовать пользователей");
        System.out.println("4. Удалить пользователей");
        System.out.println();
        System.out.println("--- Чаты ---");
        System.out.println("5. Создать приватный чат");
        System.out.println("6. Создать групповой чат");
        System.out.println("7. Создать чат-комнату");
        System.out.println("8. Показать чаты");
        System.out.println("9. Переименовать групповой чат / комнату");
        System.out.println("10. Удалить чат");
        System.out.println();
        System.out.println("--- Участники и сообщения ---");
        System.out.println("11. Добавить пользователя в чат");
        System.out.println("12. Удалить пользователя из чата");
        System.out.println("13. Забанить пользователя в чате");
        System.out.println("14. Разбанить пользователя в чате");
        System.out.println("15. Показать забаненных пользователей");
        System.out.println("16. Открыть чат и общаться");
        System.out.println("0. Выход");
        System.out.println("Выберите действие: ");
    }

    private void createUser(){
        System.out.println("Имя пользователя: ");
        User user = userService.createUser(scanner.nextLine());
    }
}
