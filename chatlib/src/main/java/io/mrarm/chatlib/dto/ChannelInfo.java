package io.mrarm.chatlib.dto;

import java.util.Date;
import java.util.List;

public class ChannelInfo {

    private String name;

    private String topic;
    private MessageSenderInfo topicSetBy;
    private Date topicSetOn;

    private List<NickWithPrefix> members;

    public ChannelInfo(String name, String topic, MessageSenderInfo topicSetBy, Date topicSetOn,
                       List<NickWithPrefix> members) {
        this.name = name;
        this.topic = topic;
        this.topicSetBy = topicSetBy;
        this.topicSetOn = topicSetOn;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public MessageSenderInfo getTopicSetBy() {
        return topicSetBy;
    }

    public Date getTopicSetOn() {
        return topicSetOn;
    }

    public List<NickWithPrefix> getMembers() {
        return members;
    }
}
