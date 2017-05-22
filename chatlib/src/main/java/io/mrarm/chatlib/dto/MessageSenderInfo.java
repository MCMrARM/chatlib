package io.mrarm.chatlib.dto;

import java.util.UUID;

public class MessageSenderInfo {

    private String nick;
    private String user;
    private String host;
    private UUID userUUID;

    public MessageSenderInfo(String nick, String user, String host, UUID userUUID) {
        this.nick = nick;
        this.user = user;
        this.host = host;
        this.userUUID = userUUID;
    }

    public String getNick() {
        return nick;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public UUID getUserUUID() {
        return userUUID;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setUserInfo(String nick, String user, String host) {
        this.nick = nick;
        this.user = user;
        this.host = host;
    }

    public void setUserUUID(UUID userUUID) {
        this.userUUID = userUUID;
    }

}
