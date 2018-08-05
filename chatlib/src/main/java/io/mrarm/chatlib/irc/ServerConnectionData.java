package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.mrarm.chatlib.ChannelListListener;
import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.cap.CapabilityManager;
import io.mrarm.chatlib.message.WritableMessageStorageApi;
import io.mrarm.chatlib.user.WritableUserInfoApi;

public class ServerConnectionData {

    private ServerConnectionApi api;
    private String userNick;
    private String userUser;
    private String userHost;
    private final HashMap<String, ChannelData> joinedChannels = new HashMap<>();
    private ServerStatusData serverStatusData = new ServerStatusData();
    private WritableUserInfoApi userInfoApi;
    private WritableMessageStorageApi messageStorageApi;
    private ChannelDataStorage channelDataStorage;
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

    public synchronized void setUserExtraInfo(String user, String host) {
        userUser = user;
        userHost = host;
    }

    public synchronized String getUserUser() {
        return userUser;
    }

    public synchronized String getUserHost() {
        return userHost;
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

    public synchronized ChannelDataStorage getChannelDataStorage() {
        return channelDataStorage;
    }

    public synchronized void setChannelDataStorage(ChannelDataStorage channelDataStorage) {
        this.channelDataStorage = channelDataStorage;
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
        String lChannelName = channelName.toLowerCase();
        synchronized (joinedChannels) {
            if (!joinedChannels.containsKey(lChannelName))
                throw new NoSuchChannelException();
            return joinedChannels.get(lChannelName);
        }
    }

    public List<String> getJoinedChannelList() {
        synchronized (joinedChannels) {
            ArrayList<String> list = new ArrayList<>();
            for (ChannelData cdata : joinedChannels.values())
                list.add(cdata.getName());
            return list;
        }
    }

    public void onChannelJoined(String channelName) {
        String lChannelName = channelName.toLowerCase();
        synchronized (joinedChannels) {
            if (joinedChannels.containsKey(lChannelName))
                return;
            ChannelData data = new ChannelData(this, channelName);
            data.loadFromStoredData();
            joinedChannels.put(lChannelName, data);
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
        String lChannelName = channelName.toLowerCase();
        synchronized (joinedChannels) {
            if (!joinedChannels.containsKey(lChannelName))
                return;
            joinedChannels.remove(lChannelName);
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
        try {
            getUserInfoApi().clearAllUsersChannelPresences(null, null).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
