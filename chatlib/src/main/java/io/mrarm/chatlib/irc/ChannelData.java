package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.List;

import io.mrarm.chatlib.MessageListener;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.user.UserInfo;

public class ChannelData {

    private String name;
    private String title;
    private List<MessageInfo> messages = new ArrayList<>();
    private List<MessageListener> messageListeners = new ArrayList<>();

    public ChannelData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(MessageInfo message) {
        messages.add(message);
        for (MessageListener listener : messageListeners)
            listener.onMessage(message);
    }

    public void subscribeMessages(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void unsubscribeMessages(MessageListener listener) {
        messageListeners.remove(listener);
    }

}
