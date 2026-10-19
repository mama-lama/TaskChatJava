package org.example.model;

public class PrivateChat extends Chat {
    public PrivateChat(int id, User firstUser, User secondUser) {
        super(id);

        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException("Для приватного чата нужны два пользователя");
        }
        if (firstUser.equals(secondUser)) {
            throw new IllegalArgumentException("Нельзя создать приватный чат пользователя с самим собой");
        }

        addUser(firstUser);
        addUser(secondUser);
    }

    @Override
    public int getMinUsers() {
        return 2;
    }

    @Override
    public int getMaxUsers() {
        return 2;
    }

    @Override
    public String getName() {
        return getUsers().get(0).getName() + " - " + getUsers().get(1).getName();
    }

    @Override
    public String getType() {
        return "Приватный чат";
    }
}
