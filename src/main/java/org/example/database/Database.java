package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:postgresql://localhost:5433/task_chat");
    private static final String USER = System.getenv().getOrDefault(
            "DB_USER", "chat_app");
    private static final String PASSWORD = System.getenv().getOrDefault(
            "DB_PASSWORD", "chat_dev_123");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id UUID PRIMARY KEY,
                        name TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS chats (
                        id INTEGER PRIMARY KEY,
                        chat_type VARCHAR(20) NOT NULL,
                        name TEXT,
                        max_users INTEGER
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS chat_users (
                        chat_id INTEGER REFERENCES chats(id) ON DELETE CASCADE,
                        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                        sort_order INTEGER NOT NULL,
                        PRIMARY KEY (chat_id, user_id)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id UUID PRIMARY KEY,
                        chat_id INTEGER REFERENCES chats(id) ON DELETE CASCADE,
                        sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
                        text TEXT NOT NULL,
                        sent_at TIMESTAMP NOT NULL,
                        sort_order INTEGER NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS chat_bans (
                        chat_id INTEGER REFERENCES chats(id) ON DELETE CASCADE,
                        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                        PRIMARY KEY (chat_id, user_id)
                    )
                    """);
        }
    }
}
