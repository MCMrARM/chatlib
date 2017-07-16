package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.dto.NickWithPrefix;

public class MultiNickPrefixParser implements NickPrefixParser {

    private static final MultiNickPrefixParser instance = new MultiNickPrefixParser();

    public static MultiNickPrefixParser getInstance() {
        return instance;
    }

    @Override
    public NickWithPrefix parse(ServerConnectionData connection, String nick) {
        NickPrefixList supportedNickPrefixes = connection.getSupportList().getSupportedNickPrefixes();
        int nl = nick.length();
        int i;
        for (i = 0; i < nl; i++) {
            if (!supportedNickPrefixes.contains(nick.charAt(i)))
                break;
        }
        if (i != 0)
            return new NickWithPrefix(nick.substring(i), new NickPrefixList(nick.substring(0, i)));
        else
            return new NickWithPrefix(nick, null);
    }

}
