package io.mrarm.chatlib.dto;

import java.util.Date;

public class MessageInfo {

    public enum MessageType {
        NORMAL, NOTICE, ME, JOIN, PART, NICK_CHANGE,
        DISCONNECT_WARNING
    }

    private MessageSenderInfo sender;
    private Date date;
    private String message;
    private MessageType type;

    public MessageInfo(MessageSenderInfo sender, Date date, String message, MessageType type) {
        this.sender = sender;
        this.date = date;
        this.message = message;
        this.type = type;
    }

    public MessageSenderInfo getSender() {
        return sender;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

}
