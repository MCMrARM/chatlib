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

}
