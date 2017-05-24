package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.user.WritableUserInfoApi;

public class ServerConnectionData {

    private String userNick;
    private HashMap<String, ChannelData> joinedChannels = new HashMap<>();
    private ServerStatusData serverStatusData = new ServerStatusData();
    private WritableUserInfoApi userInfoApi;
    private NickPrefixParser nickPrefixParser = new OneCharNickPrefixParser();

    public void setUserNick(String nick) {
        userNick = nick;
    }

    public String getUserNick() {
        return userNick;
    }

    public WritableUserInfoApi getUserInfoApi() {
        return userInfoApi;
    }

    public void setUserInfoApi(WritableUserInfoApi api) {
        this.userInfoApi = api;
    }

    public NickPrefixParser getNickPrefixParser() {
        return nickPrefixParser;
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
        joinedChannels.put(channelName, new ChannelData(this, channelName));
    }

    public ServerStatusData getServerStatusData() {
        return serverStatusData;
    }
    
}
