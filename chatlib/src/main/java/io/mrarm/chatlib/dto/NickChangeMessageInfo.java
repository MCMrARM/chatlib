package io.mrarm.chatlib.dto;

public class NickChangeMessageInfo extends MessageInfo {

    private String newNick;

    public NickChangeMessageInfo(MessageSenderInfo sender, String newNick) {
        super(sender, null, MessageType.NICK_CHANGE);
        this.newNick = newNick;
    }

    public String getNewNick() {
        return newNick;
    }

}
