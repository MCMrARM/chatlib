package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.dto.NickWithPrefix;

public class OneCharNickPrefixParser implements NickPrefixParser {

    private ServerConnectionData connection;

    public OneCharNickPrefixParser(ServerConnectionData connection) {
        this.connection = connection;
    }

    @Override
    public NickWithPrefix parse(String nick) {
        NickPrefixList supportedNickPrefixes = connection.getSupportList().getSupportedNickPrefixes();
        char firstNickChar = nick.charAt(0);
        for (char prefix : supportedNickPrefixes) {
            if (firstNickChar == prefix)
                return new NickWithPrefix(nick.substring(1), new NickPrefixList(prefix + ""));
        }
        return new NickWithPrefix(nick, null);
    }

}
