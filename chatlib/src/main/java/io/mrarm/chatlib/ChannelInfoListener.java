package io.mrarm.chatlib;

import io.mrarm.chatlib.dto.NickWithPrefix;

import java.util.List;

public interface ChannelInfoListener {

    void onMemberListChanged(List<NickWithPrefix> newMembers);

}
