package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.user.UserInfoApi;

public class ServerConnectionData {

    private String userNick;
    private HashMap<String, ChannelData> joinedChannels = new HashMap<>();
    private UserInfoApi userInfoApi;

    public void setUserNick(String nick) {
        userNick = nick;
    }

    public String getUserNick() {
        return userNick;
    }

    public UserInfoApi getUserInfoApi() {
        return userInfoApi;
    }

    public void setUserInfoProvider(UserInfoApi api) {
        this.userInfoApi = api;
    }

    public ChannelData getJoinedChannelData(String channelName) throws NoSuchChannelException {
        if (!joinedChannels.containsKey(channelName))
            throw new NoSuchChannelException();
        return joinedChannels.get(channelName);
    }

    public List<String> getJoinedChannelList() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(joinedChannels.keySet());
        return list;
    }

    public void onChannelJoined(String channelName) {
        joinedChannels.put(channelName, new ChannelData(channelName));
    }

}
