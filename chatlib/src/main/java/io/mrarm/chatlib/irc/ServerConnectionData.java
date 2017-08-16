package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mrarm.chatlib.ChannelListListener;
import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.cap.CapabilityManager;
import io.mrarm.chatlib.message.WritableMessageStorageApi;
import io.mrarm.chatlib.user.WritableUserInfoApi;

public class ServerConnectionData {

    private ServerConnectionApi api;
    private String userNick;
    private final HashMap<String, ChannelData> joinedChannels = new HashMap<>();
    private ServerStatusData serverStatusData = new ServerStatusData();
    private WritableUserInfoApi userInfoApi;
    private WritableMessageStorageApi messageStorageApi;
    private NickPrefixParser nickPrefixParser = OneCharNickPrefixParser.getInstance();
    private final ServerSupportList supportList = new ServerSupportList();
    private final MessageFilterList messageFilterList = new MessageFilterList();
    private CommandHandlerList commandHandlerList = new CommandHandlerList();
    private CapabilityManager capabilityManager = new CapabilityManager(this);
    private final List<ChannelListListener> channelListListeners = new ArrayList<>();

    public ServerConnectionData() {
        commandHandlerList.addDefaultHandlers();
        capabilityManager.addDefaultCapabilities();
    }

    public synchronized void setUserNick(String nick) {
        userNick = nick;
    }

    public synchronized String getUserNick() {
        return userNick;
    }

    public ServerConnectionApi getApi() {
        return api;
    }

    public void setApi(ServerConnectionApi api) {
        this.api = api;
    }

    public synchronized WritableUserInfoApi getUserInfoApi() {
        return userInfoApi;
    }

    public synchronized void setUserInfoApi(WritableUserInfoApi api) {
        this.userInfoApi = api;
    }

    public synchronized WritableMessageStorageApi getMessageStorageApi() {
        return messageStorageApi;
    }

    public synchronized void setMessageStorageApi(WritableMessageStorageApi messageStorageApi) {
        this.messageStorageApi = messageStorageApi;
    }

    public NickPrefixParser getNickPrefixParser() {
        return nickPrefixParser;
    }

    public void setNickPrefixParser(NickPrefixParser parser) {
        this.nickPrefixParser = parser;
    }

    public ServerSupportList getSupportList() {
        return supportList;
    }

    public CommandHandlerList getCommandHandlerList() {
        return commandHandlerList;
    }

    public CapabilityManager getCapabilityManager() {
        return capabilityManager;
    }

    public MessageFilterList getMessageFilterList() {
        return messageFilterList;
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
            if (joinedChannels.containsKey(channelName))
                return;
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

    public void onChannelLeft(String channelName) {
        synchronized (joinedChannels) {
            if (!joinedChannels.containsKey(channelName))
                return;
            joinedChannels.remove(channelName);
        }
        synchronized (channelListListeners) {
            if (channelListListeners.size() > 0) {
                List<String> joinedChannels = getJoinedChannelList();
                for (ChannelListListener listener : channelListListeners) {
                    listener.onChannelLeft(channelName);
                    listener.onChannelListChanged(joinedChannels);
                }
            }
        }
    }

    void reset() {
        synchronized (joinedChannels) {
            joinedChannels.clear();
        }
        getCapabilityManager().reset();
    }

    public void addLocalMessageToAllChannels(MessageInfo messageInfo) {
        messageStorageApi.addMessage(null, messageInfo, null, null);
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
