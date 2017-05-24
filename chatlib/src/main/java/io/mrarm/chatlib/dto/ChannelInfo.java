package io.mrarm.chatlib.dto;

import java.util.List;

public class ChannelInfo {

    private String name;

    private String topic;

    private List<NickWithPrefix> members;

    public ChannelInfo(String name, String topic, List<NickWithPrefix> members) {
        this.name = name;
        this.topic = topic;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public List<NickWithPrefix> getMembers() {
        return members;
    }
}
