package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.StatusMessageListener;
import io.mrarm.chatlib.dto.StatusMessageInfo;

import java.util.ArrayList;
import java.util.List;

public class ServerStatusData {

    private List<StatusMessageInfo> messages = new ArrayList<>();
    private String motd;
    private List<StatusMessageListener> messageListeners = new ArrayList<>();

    private String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public List<StatusMessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(StatusMessageInfo message) {
        messages.add(message);
        for (StatusMessageListener listener : messageListeners)
            listener.onStatusMessage(message);
    }

    public void subscribeMessages(StatusMessageListener listener) {
        messageListeners.add(listener);
    }

    public void unsubscribeMessages(StatusMessageListener listener) {
        messageListeners.remove(listener);
    }

}
