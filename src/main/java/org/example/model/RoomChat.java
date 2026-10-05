package org.example.model;

public class RoomChat extends Chat implements NamedChat{
    private String name;

    public RoomChat(int id, String name) {
        super(id);
        setName(name);
    }

    @Override
    public int getMinUsers(){
        return 0;
    }

    @Override
    public int getMaxUsers(){
        return Integer.MAX_VALUE;
    }

    @Override
    public String getName(){
        return name;
    }

    @Override
    public void setName(String name){
        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Название чат-комнаты не может быть пустым");
        }
        this.name = name.trim();
    }

    @Override
    public String getType(){
        return "Чат-комната";
    }
}
