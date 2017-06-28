package io.mrarm.chatlib.message;

import io.mrarm.chatlib.dto.MessageInfo;

public interface MessageListener {

    void onMessage(String channel, MessageInfo message);

}
