package org.example.model;

import java.time.LocalDateTime;

public class Message {
    private User sender;
    private String text;
    private LocalDateTime dateTime;

    public Message(User sender, String text, LocalDateTime dateTime) {
        this.sender = sender;
        this.text = text;
        this.dateTime = dateTime;
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
}
