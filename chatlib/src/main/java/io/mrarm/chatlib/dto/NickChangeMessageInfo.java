package io.mrarm.chatlib.dto;

import java.util.Date;

public class NickChangeMessageInfo extends MessageInfo {

    private String newNick;

    public NickChangeMessageInfo(MessageSenderInfo sender, Date date, String newNick) {
        super(sender, date, null, MessageType.NICK_CHANGE);
        this.newNick = newNick;
    }

    public String getNewNick() {
        return newNick;
    }

    public static class Builder extends MessageInfo.Builder {

        public Builder(MessageSenderInfo sender, String newNick) {
            messageInfo = new NickChangeMessageInfo(sender, new Date(), newNick);
        }

        public NickChangeMessageInfo build() {
            NickChangeMessageInfo ret = (NickChangeMessageInfo) messageInfo;
            messageInfo = null;
            return ret;
        }

    }

}
