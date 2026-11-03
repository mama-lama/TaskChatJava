package org.example.repository.impl;

import org.example.database.Database;
import org.example.model.*;
import org.example.repository.ChatRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcChatRepository implements ChatRepository {

    @Override
    public Chat create(Chat chat) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String sql = "INSERT INTO chats (chat_type, name, max_users, id) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    fillChatStatement(statement, chat);
                    statement.executeUpdate();
                }
                saveContents(connection, chat);
                connection.commit();
                return chat;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось создать чат", e);
        }
    }

    @Override
    public Optional<Chat> findById(Integer id) {
        try (Connection connection = Database.getConnection()) {
            return Optional.ofNullable(loadChat(connection, id));
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось найти чат", e);
        }
    }

    @Override
    public List<Chat> findAll() {
        try (Connection connection = Database.getConnection()) {
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM chats ORDER BY id");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ids.add(result.getInt("id"));
                }
            }
            List<Chat> chats = new ArrayList<>();
            for (Integer id : ids) {
                chats.add(loadChat(connection, id));
            }
            return chats;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить чаты", e);
        }
    }

    @Override
    public Chat update(Chat chat) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String sql = "UPDATE chats SET chat_type = ?, name = ?, max_users = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    fillChatStatement(statement, chat);
                    if (statement.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Чат не найден");
                    }
                }

                clearContents(connection, chat.getId());
                saveContents(connection, chat);
                connection.commit();
                return chat;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось обновить чат", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM chats WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить чат", e);
        }
    }

    private void fillChatStatement(PreparedStatement statement, Chat chat) throws SQLException {
        String type = chat instanceof PrivateChat ? "private" : chat instanceof GroupChat ? "group" : "room";
        statement.setString(1, type);
        statement.setString(2, chat instanceof PrivateChat ? null : chat.getName());
        if (chat instanceof GroupChat) {
            statement.setInt(3, chat.getMaxUsers());
        } else {
            statement.setNull(3, java.sql.Types.INTEGER);
        }
        statement.setInt(4, chat.getId());
    }

    private void clearContents(Connection connection, int chatId) throws SQLException {
        for (String table : List.of("chat_bans", "messages", "chat_users")) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE chat_id = ?")) {
                statement.setInt(1, chatId);
                statement.executeUpdate();
            }
        }
    }

    private void saveContents(Connection connection, Chat chat) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO chat_users (chat_id, user_id, sort_order) VALUES (?, ?, ?)")) {
            int position = 0;
            for (User user : chat.getUsers()) {
                statement.setInt(1, chat.getId());
                statement.setObject(2, user.getId());
                statement.setInt(3, position++);
                statement.executeUpdate();
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO messages (id, chat_id, sender_id, text, sent_at, sort_order) VALUES (?, ?, ?, ?, ?, ?)")) {
            int position = 0;
            for (Message message : chat.getMessages()) {
                statement.setObject(1, message.getId());
                statement.setInt(2, chat.getId());
                statement.setObject(3, message.getSender().getId());
                statement.setString(4, message.getText());
                statement.setTimestamp(5, Timestamp.valueOf(message.getDateTime()));
                statement.setInt(6, position++);
                statement.executeUpdate();
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO chat_bans (chat_id, user_id) VALUES (?, ?)")) {
            for (UUID userId : chat.getBannedUserIds()) {
                statement.setInt(1, chat.getId());
                statement.setObject(2, userId);
                statement.executeUpdate();
            }
        }
    }

    private Chat loadChat(Connection connection, int id) throws SQLException {
        String type;
        String name;
        int maxUsers;
        try (PreparedStatement statement = connection.prepareStatement("SELECT chat_type, name, max_users FROM chats WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                type = result.getString("chat_type");
                name = result.getString("name");
                maxUsers = result.getInt("max_users");
            }
        }

        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT u.id, u.name FROM chat_users cu
                JOIN users u ON u.id = cu.user_id
                WHERE cu.chat_id = ? ORDER BY cu.sort_order
                """)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    users.add(new User((UUID) result.getObject("id"), result.getString("name")));
                }
            }
        }

        Chat chat;
        switch (type) {
            case "private" -> chat = new PrivateChat(id, users.get(0), users.get(1));
            case "group" -> chat = new GroupChat(id, name, maxUsers, users);
            case "room" -> {
                chat = new RoomChat(id, name);
                users.forEach(chat::addUser);
            }
            default -> throw new IllegalStateException("Неизвестный тип чата: " + type);
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT user_id FROM chat_bans WHERE chat_id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    chat.banUser((UUID) result.getObject("user_id"));
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT m.id, m.text, m.sent_at, u.id AS sender_id, u.name AS sender_name
                FROM messages m JOIN users u ON u.id = m.sender_id
                WHERE m.chat_id = ? ORDER BY m.sort_order
                """)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    User sender = new User((UUID) result.getObject("sender_id"), result.getString("sender_name"));
                    Message message = new Message((UUID) result.getObject("id"), sender,
                            result.getString("text"), result.getTimestamp("sent_at").toLocalDateTime());
                    chat.addMessage(message);
                }
            }
        }
        return chat;
    }
}
