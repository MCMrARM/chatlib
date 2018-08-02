package io.mrarm.chatlib;

import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.NickWithPrefix;

import java.util.Date;
import java.util.List;

public interface ChannelInfoListener {

    void onMemberListChanged(List<NickWithPrefix> newMembers);

    void onTopicChanged(String newTopic, MessageSenderInfo newTopicSetBy, Date newTopicSetOn);

}
