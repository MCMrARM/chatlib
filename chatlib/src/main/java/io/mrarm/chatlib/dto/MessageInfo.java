package io.mrarm.chatlib.dto;

public class MessageInfo {

    public enum MessageType {
        NORMAL, NOTICE, ME, JOIN, PART
    }

    private String senderNick;
    private String message;
    private MessageType type;

    public MessageInfo(String senderNick, String message, MessageType type) {
        this.senderNick = senderNick;
        this.message = message;
        this.type = type;
    }

    public String getSenderNick() {
        return senderNick;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

}
