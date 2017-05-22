package io.mrarm.chatlib.dto;

import java.util.List;

public class MessageList {

    private List<MessageInfo> messages;

    public MessageList(List<MessageInfo> messages) {
        this.messages = messages;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

}
