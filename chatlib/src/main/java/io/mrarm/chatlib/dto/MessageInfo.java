package io.mrarm.chatlib.dto;

public class MessageInfo {

    public enum MessageType {
        NORMAL, NOTICE, ME, JOIN, PART
    }

    private MessageSenderInfo sender;
    private String message;
    private MessageType type;

    public MessageInfo(MessageSenderInfo sender, String message, MessageType type) {
        this.sender = sender;
        this.message = message;
        this.type = type;
    }

    public MessageSenderInfo getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

}
