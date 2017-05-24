package io.mrarm.chatlib.dto;

public class NickWithPrefix {

    private String nick;
    private NickPrefixList nickPrefixes;

    public NickWithPrefix(String nick, NickPrefixList nickPrefixes) {
        this.nick = nick;
        this.nickPrefixes = nickPrefixes;
    }

    public String getNick() {
        return nick;
    }

    public NickPrefixList getNickPrefixes() {
        return nickPrefixes;
    }

    @Override
    public String toString() {
        return (nickPrefixes != null ? nickPrefixes.toString() : "") + nick;
    }
}
