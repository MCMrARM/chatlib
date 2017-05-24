package io.mrarm.chatlib.dto;

import java.util.List;

public class StatusMessageList {

    private List<StatusMessageInfo> messages;

    public StatusMessageList(List<StatusMessageInfo> messages) {
        this.messages = messages;
    }

    public List<StatusMessageInfo> getMessages() {
        return messages;
    }

}
