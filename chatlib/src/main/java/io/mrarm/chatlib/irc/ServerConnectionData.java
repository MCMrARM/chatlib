package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mrarm.chatlib.NoSuchChannelException;

public class ServerConnectionData {

    private String userNick;
    private HashMap<String, ChannelData> joinedChannels = new HashMap<>();

    public void setUserNick(String nick) {
        userNick = nick;
    }

    public String getUserNick() {
        return userNick;
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
