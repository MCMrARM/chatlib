package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mrarm.chatlib.ChannelListListener;
import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.user.WritableUserInfoApi;

public class ServerConnectionData {

    private String userNick;
    private HashMap<String, ChannelData> joinedChannels = new HashMap<>();
    private ServerStatusData serverStatusData = new ServerStatusData();
    private WritableUserInfoApi userInfoApi;
    private NickPrefixParser nickPrefixParser = new OneCharNickPrefixParser(this);
    private NickPrefixList supportedNickPrefixes = new NickPrefixList("@+");
    private List<ChannelListListener> channelListListeners = new ArrayList<>();

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

    public NickPrefixList getSupportedNickPrefixes() {
        return supportedNickPrefixes;
    }

    public ChannelData getJoinedChannelData(String channelName) throws NoSuchChannelException {
        synchronized (joinedChannels) {
            if (!joinedChannels.containsKey(channelName))
                throw new NoSuchChannelException();
            return joinedChannels.get(channelName);
        }
    }

    public List<String> getJoinedChannelList() {
        synchronized (joinedChannels) {
            ArrayList<String> list = new ArrayList<>();
            list.addAll(joinedChannels.keySet());
            return list;
        }
    }

    public void onChannelJoined(String channelName) {
        synchronized (joinedChannels) {
            joinedChannels.put(channelName, new ChannelData(this, channelName));
        }
        synchronized (channelListListeners) {
            if (channelListListeners.size() > 0) {
                List<String> joinedChannels = getJoinedChannelList();
                for (ChannelListListener listener : channelListListeners) {
                    listener.onChannelJoined(channelName);
                    listener.onChannelListChanged(joinedChannels);
                }
            }
        }
    }

    public ServerStatusData getServerStatusData() {
        return serverStatusData;
    }

    public void subscribeChannelList(ChannelListListener listener) {
        channelListListeners.add(listener);
    }

    public void unsubscribeChannelList(ChannelListListener listener) {
        channelListListeners.remove(listener);
    }
    
}
