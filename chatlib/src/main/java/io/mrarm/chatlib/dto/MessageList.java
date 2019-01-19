package io.mrarm.chatlib.dto;

import java.util.List;

public class MessageList {

    private List<MessageInfo> messages;
    private MessageListAfterIdentifier newer;
    private MessageListAfterIdentifier older;

    public MessageList(List<MessageInfo> messages, MessageListAfterIdentifier newer, MessageListAfterIdentifier older) {
        this.messages = messages;
        this.newer = newer;
        this.older = older;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public MessageListAfterIdentifier getNewer() {
        return newer;
    }

    public MessageListAfterIdentifier getOlder() {
        return older;
    }
}
