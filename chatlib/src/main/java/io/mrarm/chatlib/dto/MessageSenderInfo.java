package io.mrarm.chatlib.dto;

import java.util.UUID;

public class MessageSenderInfo {

    private String nick;
    private String user;
    private String host;
    private NickPrefixList nickPrefixes;
    private UUID userUUID;

    public MessageSenderInfo(String nick, String user, String host, NickPrefixList nickPrefixes, UUID userUUID) {
        this.nick = nick;
        this.user = user;
        this.host = host;
        this.nickPrefixes = nickPrefixes;
        this.userUUID = userUUID;
    }

    public String getNick() {
        return nick;
    }

    public NickPrefixList getNickPrefixes() {
        return nickPrefixes;
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

}
