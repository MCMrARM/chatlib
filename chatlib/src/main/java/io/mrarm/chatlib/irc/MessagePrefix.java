package io.mrarm.chatlib.irc;

public class MessagePrefix {

    private String prefix;

    public MessagePrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }

    // sent by server

    public String getServerName() {
        return prefix;
    }

    // sent by user

    public String getNick() {
        int iof = prefix.indexOf('!');
        if (iof != -1)
            return prefix.substring(0, iof);
        iof = prefix.indexOf('@');
        if (iof != -1)
            return prefix.substring(0, iof);
        return prefix;
    }

    public String getUser() {
        int iof = prefix.indexOf('!');
        if (iof == -1)
            return null;
        int iof2 = prefix.indexOf('@');
        if (iof2 != -1)
            return prefix.substring(iof + 1, iof2);
        return prefix.substring(iof + 1);
    }

    public String getHost() {
        int iof = prefix.indexOf('@');
        if (iof == -1)
            return null;
        return prefix.substring(iof + 1);
    }

}
