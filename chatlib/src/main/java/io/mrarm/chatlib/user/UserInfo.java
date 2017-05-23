package io.mrarm.chatlib.user;

import java.util.UUID;

public class UserInfo {

    private UUID uuid;
    private String currentNick;
    private boolean connected = false;

    public UserInfo(UUID uuid, String nick) {
        this.uuid = uuid;
        this.currentNick = nick;
    }

    void setCurrentNick(String nick) {
        currentNick = nick;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getCurrentNick() {
        return currentNick;
    }

    public boolean isConnected() {
        return connected;
    }

}
