package io.mrarm.chatlib.dto;

import java.util.Date;

public class StatusMessageInfo {

    public enum MessageType {
        MOTD
    }

    private String sender;
    private Date date;
    private MessageType type;
    private String message;

    public StatusMessageInfo(String sender, Date date, MessageType type, String message) {
        this.sender = sender;
        this.date = date;
        this.type = type;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public Date getDate() {
        return date;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
