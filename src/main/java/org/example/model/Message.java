package org.example.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Message {
    private final UUID id;
    private final User sender;
    private String text;
    private final LocalDateTime dateTime;

    public Message(UUID id, User sender, String text, LocalDateTime dateTime) {
        this.id = Objects.requireNonNull(id, "Индификатор сообщения не может быть нулевым");
        this.sender = Objects.requireNonNull(sender, "Отправитель не может быть нулевым");
        this.dateTime = Objects.requireNonNull(dateTime, "Дата сообщения не может быть нулевым");
        setText(text);
    }

    public UUID getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }
        this.text = text.trim();
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
