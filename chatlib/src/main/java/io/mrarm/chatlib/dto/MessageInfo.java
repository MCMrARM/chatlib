package io.mrarm.chatlib.dto;

import java.util.Date;

public class MessageInfo {

    public enum MessageType {
        NORMAL(0), NOTICE(1), ME(2), JOIN(3), PART(4), QUIT(5), NICK_CHANGE(6), MODE(7), TOPIC(8), TOPIC_WHOTIME(10),
        KICK(9),
        DISCONNECT_WARNING(100);

        private final int intValue;
        MessageType(int i) {
            intValue = i;
        }

        public int asInt() {
            return intValue;
        }
    }

    private MessageSenderInfo sender;
    private Date date;
    private String message;
    private MessageType type;
    private BatchInfo batch;

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

    public BatchInfo getBatch() {
        return batch;
    }

    public static class Builder {

        protected MessageInfo messageInfo;

        public Builder() {
            messageInfo = new MessageInfo(null, new Date(), null, null);
        }

        public Builder(MessageSenderInfo info, String message, MessageType type) {
            messageInfo = new MessageInfo(info, new Date(), message, type);
        }

        public void setSender(MessageSenderInfo sender) {
            messageInfo.sender = sender;
        }

        public void setMessage(String message) {
            messageInfo.message = message;
        }

        public void setDate(Date date) {
            messageInfo.date = date;
        }

        public void setType(MessageType type) {
            messageInfo.type = type;
        }

        public void setBatch(BatchInfo batch) {
            messageInfo.batch = batch;
        }

        public MessageInfo build() {
            MessageInfo ret = messageInfo;
            messageInfo = null;
            return ret;
        }

    }

}
