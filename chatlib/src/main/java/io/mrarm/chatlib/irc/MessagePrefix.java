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
        String ret = prefix.substring(iof + 1);
        iof = prefix.indexOf('@');
        if (iof != -1)
            return ret.substring(0, iof);
        return ret;
    }

    public String getHost() {
        int iof = prefix.indexOf('@');
        if (iof == -1)
            return null;
        return prefix.substring(iof + 1);
    }

}
