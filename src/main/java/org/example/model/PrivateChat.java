package org.example.model;

public class PrivateChat extends Chat{
    public PrivateChat(int id, User user1, User user2) throws IllegalAccessException {
        super(id);

        if (user1.equals(user2)){
            throw new IllegalAccessException(
                    "Нельзя создавать приватный чат с самим собой!"
            );
        }

        if(user1 == null || user2 == null){
            throw new IllegalAccessException("Для приватного чата нужны два пользователя!");
        }
        addUser(user1);
        addUser(user2);
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
        return getUsers().get(0).getName() + " " + getUsers().get(1).getName();
    }

    @Override
    public String getType() {
        return "Приватный чат";
    }
}
