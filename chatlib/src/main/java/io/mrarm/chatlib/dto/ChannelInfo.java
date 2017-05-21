package io.mrarm.chatlib.dto;

public class ChannelInfo {

    private String name;

    private String title;

    public ChannelInfo(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

}
