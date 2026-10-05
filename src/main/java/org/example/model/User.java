package org.example.model;

import java.util.UUID;
import java.util.Objects;

public class User {
    private final UUID id;
    private String name;

    public User(UUID id, String name) {
        this.id = id;
        setName(name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if (!(obj instanceof User user)){
            return false;
        }
        return id.equals(user.id);
    }

    @Override
    public int hasCode(){
        return id.hashCode();
    }

    @Override
    public String toString(){
        return name;
    }
}