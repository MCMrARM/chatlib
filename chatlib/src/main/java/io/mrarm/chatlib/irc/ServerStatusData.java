package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.StatusMessageListener;
import io.mrarm.chatlib.dto.StatusMessageInfo;

import java.util.ArrayList;
import java.util.List;

public class ServerStatusData {

    private final List<StatusMessageInfo> messages = new ArrayList<>();
    private String motd;
    private List<StatusMessageListener> messageListeners = new ArrayList<>();

    private String getMotd() {
        synchronized (this) {
            return motd;
        }
    }

    public void setMotd(String motd) {
        synchronized (this) {
            this.motd = motd;
        }
    }

    public List<StatusMessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(StatusMessageInfo message) {
        synchronized (messages) {
            messages.add(message);
        }
        synchronized (messageListeners) {
            for (StatusMessageListener listener : messageListeners)
                listener.onStatusMessage(message);
        }
    }

    public void subscribeMessages(StatusMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }

    public void unsubscribeMessages(StatusMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.remove(listener);
        }
    }

}
