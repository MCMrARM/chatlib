package io.mrarm.chatlib.dto;

import java.util.List;

public class MessageList {

    private List<MessageInfo> messages;
    private List<MessageId> messageIds;
    private MessageListAfterIdentifier newer;
    private MessageListAfterIdentifier older;

    public MessageList(List<MessageInfo> messages, List<MessageId> messageIds,
                       MessageListAfterIdentifier newer, MessageListAfterIdentifier older) {
        this.messages = messages;
        this.messageIds = messageIds;
        this.newer = newer;
        this.older = older;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public List<MessageId> getMessageIds() {
        return messageIds;
    }

    public MessageListAfterIdentifier getNewer() {
        return newer;
    }

    public MessageListAfterIdentifier getOlder() {
        return older;
    }
}
