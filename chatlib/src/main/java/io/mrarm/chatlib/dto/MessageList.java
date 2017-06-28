package io.mrarm.chatlib.dto;

import java.util.List;

public class MessageList {

    private List<MessageInfo> messages;
    private MessageListAfterIdentifier after;

    public MessageList(List<MessageInfo> messages, MessageListAfterIdentifier after) {
        this.messages = messages;
        this.after = after;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public MessageListAfterIdentifier getAfterIdentifier() {
        return after;
    }
}
