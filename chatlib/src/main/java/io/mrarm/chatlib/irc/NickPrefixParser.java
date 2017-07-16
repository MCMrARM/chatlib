package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.NickWithPrefix;

public interface NickPrefixParser {

    NickWithPrefix parse(ServerConnectionData connection, String nick);

}
