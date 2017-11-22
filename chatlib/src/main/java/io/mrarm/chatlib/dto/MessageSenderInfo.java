package io.mrarm.chatlib.dto;

import java.util.UUID;

public class MessageSenderInfo extends NickWithPrefix {

    private String user;
    private String host;
    private UUID userUUID;

    public MessageSenderInfo(String nick, String user, String host, NickPrefixList nickPrefixes, UUID userUUID) {
        super(nick, nickPrefixes);
        this.user = user;
        this.host = host;
        this.userUUID = userUUID;
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
