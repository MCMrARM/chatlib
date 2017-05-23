package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.dto.NickWithPrefix;

public class OneCharNickPrefixParser implements NickPrefixParser {

    @Override
    public NickWithPrefix parse(String nick) {
        return new NickWithPrefix(nick, new NickPrefixList("")); // TODO:
    }

}
