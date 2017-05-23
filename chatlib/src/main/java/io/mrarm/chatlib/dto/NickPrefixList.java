package io.mrarm.chatlib.dto;

public class NickPrefixList {

    private String prefix;

    public NickPrefixList(String prefix) {
        this.prefix = prefix;
    }

    public int length() {
        return prefix.length();
    }

    public char get(int i) {
        return prefix.charAt(i);
    }

    @Override
    public String toString() {
        return prefix;
    }


}
