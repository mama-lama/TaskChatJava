package org.example.controller;

import org.example.model.Chat;
import org.example.model.GroupChat;
import org.example.model.Message;
import org.example.model.User;
import org.example.service.ChatService;
import org.example.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final UserService userService;
    private final ChatService chatService;
    private final Scanner scanner;

    public ConsoleController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Консольный чат ===");
        boolean running = true;

        while (running) {
            printMainMenu();
            String command = scanner.nextLine().trim();

            try {
                switch (command) {
                    case "1" -> createUser();
                    case "2" -> showUsers();
                    case "3" -> renameUser();
                    case "4" -> deleteUser();
                    case "5" -> createPrivateChat();
                    case "6" -> createGroupChat();
                    case "7" -> createRoom();
                    case "8" -> showChats();
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
            } catch (RuntimeException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        System.out.println("Программа завершена");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("--- Пользователи ---");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Показать пользователей");
        System.out.println("3. Переименовать пользователя");
        System.out.println("4. Удалить пользователя");
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
        System.out.print("Выберите действие: ");
    }

    private void createUser() {
        System.out.print("Имя пользователя: ");
        User user = userService.createUser(scanner.nextLine());
        System.out.println("Создан пользователь: " + user.getName());
    }

    private void showUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователей пока нет");
            return;
        }

        System.out.println("Пользователи:");
        for (int i = 0; i < users.size(); i++) {
            System.out.printf("%d. %s | id: %s%n", i + 1, users.get(i).getName(), users.get(i).getId());
        }
    }

    private void renameUser() {
        User user = chooseUser("Кого переименовать");
        System.out.print("Новое имя: ");
        userService.renameUser(user.getId(), scanner.nextLine());
        System.out.println("Имя изменено");
    }

    private void deleteUser() {
        User user = chooseUser("Кого удалить");
        userService.deleteUser(user.getId());
        System.out.println("Пользователь удалён");
        System.out.println("Если после удаления нарушался минимальный состав приватного/группового чата, такой чат удалён целиком.");
    }

    private void createPrivateChat(){
        requireUsers(2, "Для приватного чата сначала создайте минимум двух пользователей");

        System.out.println("Выберите первого пользователя:");
        User first = chooseUserFromList(userService.getAllUsers());

        List<User> remaining = new ArrayList<>(userService.getAllUsers());
        remaining.remove(first);
        System.out.println("Выберите второго пользователя:");
        User second = chooseUserFromList(remaining);

        Chat chat = null;
        try {
            chat = chatService.createPrivateChat(first.getId(), second.getId());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Создан приватный чат '%s'. ID = %d%n", chat.getName(), chat.getId());
    }

    private void createGroupChat() {
        requireUsers(3, "Для группового чата сначала создайте минимум трёх пользователей");

        System.out.print("Название группового чата: ");
        String name = scanner.nextLine();

        System.out.print("Максимальное число участников (не меньше 3): ");
        int maxUsers = readInt();
        if (maxUsers < 3) {
            throw new IllegalArgumentException("Максимальное число участников должно быть не меньше 3");
        }

        int availableCount = userService.getAllUsers().size();
        int maxInitialCount = Math.min(maxUsers, availableCount);
        System.out.printf("Сколько пользователей добавить сразу (от 3 до %d): ", maxInitialCount);
        int initialCount = readInt();
        if (initialCount < 3 || initialCount > maxInitialCount) {
            throw new IllegalArgumentException("Неверное количество начальных участников");
        }

        List<UUID> userIds = chooseDifferentUsers(initialCount);
        Chat chat = chatService.createGroupChat(name, maxUsers, userIds);
        System.out.printf("Создан групповой чат '%s'. ID = %d%n", chat.getName(), chat.getId());
    }

    private void createRoom() {
        System.out.print("Название чат-комнаты: ");
        Chat room = chatService.createRoom(scanner.nextLine());
        System.out.println("Чат-комната создана. ID = " + room.getId());
        System.out.println("Количество участников комнаты не ограничено. Добавить их можно через пункт 11.");
    }

    private void showChats() {
        List<Chat> chats = chatService.getAllChats();
        if (chats.isEmpty()) {
            System.out.println("Чатов пока нет");
            return;
        }

        System.out.println("Чаты:");
        for (Chat chat : chats) {
            String limit = chat.getMaxUsers() == Integer.MAX_VALUE
                    ? "без ограничений"
                    : String.valueOf(chat.getMaxUsers());

            System.out.printf("ID %d | %s | %s | участников: %d/%s | сообщений: %d%n",
                    chat.getId(), chat.getType(), chat.getName(),
                    chat.getUsers().size(), limit, chat.getMessages().size());

            if (chat.getUsers().isEmpty()) {
                System.out.println("   Участники: нет");
            } else {
                System.out.print("   Участники: ");
                for (int i = 0; i < chat.getUsers().size(); i++) {
                    User user = chat.getUsers().get(i);
                    if (i > 0) {
                        System.out.print(", ");
                    }
                    System.out.print(user.getName());
                    if (chat.isBanned(user.getId())) {
                        System.out.print(" [BAN]");
                    }
                }
                System.out.println();
            }
        }
    }

    private void renameChat() {
        Chat chat = chooseChat();
        System.out.print("Новое название: ");
        chatService.renameChat(chat.getId(), scanner.nextLine());
        System.out.println("Название изменено");
    }

    private void deleteChat() {
        Chat chat = chooseChat();
        chatService.deleteChat(chat.getId());
        System.out.println("Чат удалён");
    }

    private void addUserToChat() {
        Chat chat = chooseChat();

        List<User> candidates = userService.getAllUsers().stream()
                .filter(user -> !chat.containsUser(user.getId()))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("Нет пользователей, которых можно добавить в этот чат");
        }

        User user = chooseUserFromList(candidates);
        chatService.addUserToChat(chat.getId(), user.getId());
        System.out.println(user.getName() + " добавлен в чат " + chat.getName());
    }

    private void removeUserFromChat() {
        Chat chat = chooseChat();
        if (chat.getUsers().isEmpty()) {
            throw new IllegalStateException("В чате нет пользователей");
        }

        User user = chooseChatUser(chat, "Кого удалить из чата");
        chatService.removeUserFromChat(chat.getId(), user.getId());
        System.out.println(user.getName() + " удалён из чата");
    }

    private void banUser() {
        Chat chat = chooseChat();
        if (chat.getUsers().isEmpty()) {
            throw new IllegalStateException("В чате нет пользователей");
        }

        User user = chooseChatUser(chat, "Кого забанить");
        chatService.banUser(chat.getId(), user.getId());
        System.out.println(user.getName() + " больше не может отправлять сообщения в этом чате");
    }

    private void unbanUser() {
        Chat chat = chooseChat();
        List<User> bannedUsers = chatService.getBannedUsers(chat.getId());
        if (bannedUsers.isEmpty()) {
            throw new IllegalStateException("В этом чате нет забаненных пользователей");
        }

        System.out.println("Кого разбанить:");
        User user = chooseUserFromList(bannedUsers);
        chatService.unbanUser(chat.getId(), user.getId());
        System.out.println(user.getName() + " снова может отправлять сообщения в этом чате");
    }

    private void showBannedUsers() {
        Chat chat = chooseChat();
        List<User> bannedUsers = chatService.getBannedUsers(chat.getId());

        if (bannedUsers.isEmpty()) {
            System.out.println("В чате '" + chat.getName() + "' нет забаненных пользователей");
            return;
        }

        System.out.println("Забанены в чате '" + chat.getName() + "':");
        bannedUsers.forEach(user -> System.out.println("- " + user.getName()));
    }

    private void openChat() {
        Chat chat = chooseChat();
        if (chat.getUsers().size() < 2) {
            System.out.println("Для общения через одну консоль добавьте в чат хотя бы двух пользователей");
            return;
        }

        System.out.println();
        System.out.println("=== " + chat.getName() + " ===");
        printHistory(chat.getId());
        System.out.println("Для выхода из чата введите 0 при выборе пользователя.");

        while (true) {
            Chat actualChat = chatService.getChat(chat.getId());
            List<User> users = actualChat.getUsers();

            System.out.println();
            System.out.println("Кто пишет?");
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                String status = actualChat.isBanned(user.getId()) ? " [BAN]" : "";
                System.out.printf("%d. %s%s%n", i + 1, user.getName(), status);
            }
            System.out.println("0. Вернуться в главное меню");
            System.out.print("Выберите пользователя: ");

            int choice = readInt();
            if (choice == 0) {
                return;
            }
            if (choice < 1 || choice > users.size()) {
                System.out.println("Нет такого пользователя");
                continue;
            }

            User sender = users.get(choice - 1);
            System.out.print(sender.getName() + ": ");
            String text = scanner.nextLine();

            try {
                Message message = chatService.sendMessage(actualChat.getId(), sender.getId(), text);
                System.out.printf("[%s] %s: %s%n",
                        message.getDateTime().format(TIME_FORMAT),
                        message.getSender().getName(),
                        message.getText());
            } catch (RuntimeException e) {
                System.out.println("Ошибка отправки: " + e.getMessage());
            }
        }
    }

    private void printHistory(int chatId) {
        List<Message> messages = chatService.getMessages(chatId);
        if (messages.isEmpty()) {
            System.out.println("История сообщений пуста");
            return;
        }

        System.out.println("История:");
        for (Message message : messages) {
            System.out.printf("[%s] %s: %s%n",
                    message.getDateTime().format(TIME_FORMAT),
                    message.getSender().getName(),
                    message.getText());
        }
    }

    private User chooseUser(String title) {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            throw new IllegalStateException("Сначала создайте пользователя");
        }

        System.out.println(title + ":");
        return chooseUserFromList(users);
    }

    private User chooseUserFromList(List<User> users) {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Список пользователей пуст");
        }

        for (int i = 0; i < users.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, users.get(i).getName());
        }
        System.out.print("Номер: ");
        int choice = readInt();

        if (choice < 1 || choice > users.size()) {
            throw new IllegalArgumentException("Неверный номер пользователя");
        }
        return users.get(choice - 1);
    }

    private List<UUID> chooseDifferentUsers(int count) {
        List<User> available = new ArrayList<>(userService.getAllUsers());
        List<UUID> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            System.out.printf("Выберите участника %d из %d:%n", i, count);
            User user = chooseUserFromList(available);
            result.add(user.getId());
            available.remove(user);
        }

        return result;
    }

    private User chooseChatUser(Chat chat, String title) {
        System.out.println(title + ":");
        return chooseUserFromList(chat.getUsers());
    }

    private Chat chooseChat() {
        List<Chat> chats = chatService.getAllChats();
        if (chats.isEmpty()) {
            throw new IllegalStateException("Сначала создайте чат");
        }

        System.out.println("Выберите чат:");
        for (int i = 0; i < chats.size(); i++) {
            Chat chat = chats.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, chat.getName(), chat.getType());
        }
        System.out.print("Номер: ");
        int choice = readInt();

        if (choice < 1 || choice > chats.size()) {
            throw new IllegalArgumentException("Неверный номер чата");
        }
        return chats.get(choice - 1);
    }

    private void requireUsers(int count, String message) {
        if (userService.getAllUsers().size() < count) {
            throw new IllegalStateException(message);
        }
    }

    private int readInt() {
        String value = scanner.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Нужно ввести число");
        }
    }
}
