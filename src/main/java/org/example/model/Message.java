package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class Message {
    private final UUID id;
    private final User sender;
    private String text;
    private final LocalDateTime dateTime;

    public Message(User sender, String text, UUID id, LocalDateTime dateTime) {
        this.id = Objects.requireNonNull(id, "id не может быть null");
        this.sender = Objects.requireNonNull(sender, "Отправитель не может быть null");
        this.dateTime = Objects.requireNonNull(dateTime, "Дата на может быть null");
    }

    public User getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public UUID getId(){ return id; }

    public void setText(String text){
        if(text == null || text.isBlank()){
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }
        this.text = text.trim();
    }
}
