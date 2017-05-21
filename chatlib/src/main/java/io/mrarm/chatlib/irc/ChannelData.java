package io.mrarm.chatlib.irc;

import java.util.ArrayList;

import io.mrarm.chatlib.dto.MessageInfo;

public class ChannelData {

    private String name;
    private String title;
    private ArrayList<MessageInfo> messages = new ArrayList<>();

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

    public ArrayList<MessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(MessageInfo message) {
        messages.add(message);
    }

}
