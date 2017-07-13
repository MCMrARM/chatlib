package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.NickPrefixList;

public class ServerSupportList {

    private NickPrefixList nickPrefixes = new NickPrefixList("@+");
    private char[] nickPrefixModes = new char[] { 'o', 'v' };

    public NickPrefixList getSupportedNickPrefixes() {
        synchronized (this) {
            return nickPrefixes;
        }
    }

    public char[] getSupportedNickPrefixModes() {
        synchronized (this) {
            return nickPrefixModes;
        }
    }

    public void setSupportedNickPrefixes(NickPrefixList supportedNickPrefixes) {
        synchronized (this) {
            this.nickPrefixes = supportedNickPrefixes;
        }
    }

    public void setSupportedNickPrefixModes(char[] nickPrefixModes) {
        synchronized (this) {
            this.nickPrefixModes = nickPrefixModes;
        }
    }
}
