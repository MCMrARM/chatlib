package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.MessageSenderInfo;

import java.util.UUID;

public class MessagePrefix {

    private String rawPrefix;
    private String nick;
    private String user;
    private String host;

    public MessagePrefix(String prefix) {
        this.rawPrefix = prefix;
        this.nick = rawPrefix;
        int iof = prefix.indexOf('!');
        int iof2 = prefix.indexOf('@', (iof == -1 ? 0 : iof + 1));
        if (iof != -1 || iof2 != -1)
            this.nick = prefix.substring(0, (iof != -1 ? iof : iof2));
        if (iof != -1)
            this.user = prefix.substring(iof + 1, (iof2 == -1 ? prefix.length() : iof2));
        if (iof2 != -1)
            this.host = prefix.substring(iof2 + 1);
    }

    @Override
    public String toString() {
        return rawPrefix;
    }

    public MessageSenderInfo toSenderInfo(UUID userUUID, ChannelData channelData) {
        ChannelData.Member memberInfo = channelData != null && userUUID != null ? channelData.getMember(userUUID) : null;
        return new MessageSenderInfo(getNick(), getUser(), getHost(),
                memberInfo != null ? memberInfo.getNickPrefixes() : null, userUUID);
    }

    // sent by server

    public String getServerName() {
        return rawPrefix;
    }

    // sent by user

    public String getNick() {
        return nick;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

}
