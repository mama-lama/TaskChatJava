package org.example.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Chat {
    protected final int id;
    protected final List<User> users;
    protected final List<Message> messages;
    protected final Set<User> bannedUsers;

    public Chat(int id) {
        this.id = id;
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.bannedUsers = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public List<User> getUsers() {
        return users;
    }
}
