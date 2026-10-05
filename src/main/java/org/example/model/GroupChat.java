package org.example.model;

import java.util.List;

public class GroupChat extends Chat implements NamedChat{
    private String name;
    private final int maxUsers;

    public GroupChat(int id, String name, int maxUsers, List<User> users) {
        super(id);
        if (maxUsers < 3){
            throw new IllegalArgumentException("Лимит группового чата должен быть не меньше 3");
        }
        if(users == null || users.size() < 3){
            throw new IllegalArgumentException("Для группового чата нужно минимум 3 пользователя");
        }
        if (users.size() > maxUsers){
            throw new IllegalArgumentException("Количество пользователей больше лимита чата");
        }

        this.maxUsers = maxUsers;
        setName(name);
        users.forEach(this::addUser);
    }

    @Override
    public int getMinUsers(){
        return 3;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "Групповой чат";
    }

    @Override
    public int getMaxUsers() {
        return maxUsers;
    }

    @Override
    public void setName(String name){
        if (name == null ||name.isBlank()){
            throw new IllegalArgumentException("Имя группового чата ен может быть пустым");
        }
        this.name = name.trim();
    }
}
