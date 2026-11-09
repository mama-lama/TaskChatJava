package org.example.repository.impl;

import org.example.database.Database;
import org.example.model.User;
import org.example.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserRepository implements UserRepository {
    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, user.getId());
            statement.setString(2, user.getName());
            statement.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT id, name FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(new User((UUID) result.getObject("id"), result.getString("name")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось найти пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name FROM users ORDER BY name, id";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(new User((UUID) result.getObject("id"), result.getString("name")));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось получить пользователей", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setObject(2, user.getId());
            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("Пользователь не найден");
            }
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось изменить пользователя", e);
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }
}
