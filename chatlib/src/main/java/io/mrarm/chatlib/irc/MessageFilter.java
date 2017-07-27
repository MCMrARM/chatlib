package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.MessageInfo;

public interface MessageFilter {

    boolean filter(ServerConnectionData connection, String channel, MessageInfo messageInfo);

}
