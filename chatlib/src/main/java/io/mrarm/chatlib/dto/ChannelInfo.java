package io.mrarm.chatlib.dto;

import java.util.List;

public class ChannelInfo {

    private String name;

    private String title;

    private List<NickWithPrefix> members;

    public ChannelInfo(String name, String title, List<NickWithPrefix> members) {
        this.name = name;
        this.title = title;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<NickWithPrefix> getMembers() {
        return members;
    }
}
