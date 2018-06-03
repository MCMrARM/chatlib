package io.mrarm.chatlib.dto;

import java.util.Date;

public class KickMessageInfo extends MessageInfo {

    private String kickedNick;

    public KickMessageInfo(MessageSenderInfo sender, Date date, String kickedNick, String message) {
        super(sender, date, message, MessageType.KICK);
        this.kickedNick = kickedNick;
    }

    public String getKickedNick() {
        return kickedNick;
    }

    public static class Builder extends MessageInfo.Builder {

        public Builder(MessageSenderInfo sender, String kickedNick, String message) {
            messageInfo = new KickMessageInfo(sender, new Date(), kickedNick, message);
        }

        public KickMessageInfo build() {
            KickMessageInfo ret = (KickMessageInfo) messageInfo;
            messageInfo = null;
            return ret;
        }

    }

}
