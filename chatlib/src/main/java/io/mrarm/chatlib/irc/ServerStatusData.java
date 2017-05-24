package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.StatusMessageInfo;

import java.util.ArrayList;
import java.util.List;

public class ServerStatusData {

    private List<StatusMessageInfo> messages = new ArrayList<>();
    private String motd;

    private String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public List<StatusMessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(StatusMessageInfo info) {
        messages.add(info);
    }

}
