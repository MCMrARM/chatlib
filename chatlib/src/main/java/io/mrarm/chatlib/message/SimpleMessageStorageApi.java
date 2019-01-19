package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageFilterOptions;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.*;
import java.util.concurrent.Future;

public class SimpleMessageStorageApi implements WritableMessageStorageApi {

    private final Map<String, ChannelData> channels = new HashMap<>();
    private final List<MessageListener> globalListeners = new ArrayList<>();

    private ChannelData getChannelData(String channelName) {
        ChannelData channel = channels.get(channelName);
        if (channel == null) {
            channel = new ChannelData();
            channels.put(channelName, channel);
        }
        return channel;
    }

    @Override
    public Future<Void> addMessage(String channelName, MessageInfo message, ResponseCallback<Void> callback,
                                   ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (channels) {
                ChannelData channel = getChannelData(channelName);
                channel.messages.add(message);
                for (MessageListener listener : channel.listeners)
                    listener.onMessage(channelName, message);
            }
            synchronized (globalListeners) {
                for (MessageListener listener : globalListeners)
                    listener.onMessage(channelName, message);
            }
            return null;
        }, callback, errorCallback);
    }

    public static boolean isMessageIncludedInFilter(MessageInfo msg, MessageFilterOptions filter) {
        if (filter == null)
            return true;
        if (filter.excludeMessageTypes != null && filter.excludeMessageTypes.contains(msg.getType()))
            return false;
        if (filter.restrictToMessageTypes != null && !filter.restrictToMessageTypes.contains(msg.getType()))
            return false;
        return true;
    }

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageFilterOptions filter,
                                           MessageListAfterIdentifier after, ResponseCallback<MessageList> callback,
                                           ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (channels) {
                ChannelData data = getChannelData(channelName);
                List<MessageInfo> ret = new ArrayList<>();
                int end = data.messages.size();
                if (after != null && after instanceof SimpleMessageListAfterIdentifier)
                    end = ((SimpleMessageListAfterIdentifier) after).getIndex();
                int start = Math.max(end - count, 0);
                if (filter == null) {
                    for (int i = start; i < end; i++)
                        ret.add(data.messages.get(i));
                } else {
                    ret = new ArrayList<>();
                    int n = count;
                    for (start = end - 1; start >= 0; --start) {
                        if (!isMessageIncludedInFilter(data.messages.get(start), filter))
                            continue;
                        ret.add(data.messages.get(start));
                        if (--n == 0)
                            break;
                    }
                    Collections.reverse(ret);
                }
                return new MessageList(ret, new SimpleMessageListAfterIdentifier(start));
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeChannelMessages(String channelName, MessageListener listener,
                                                 ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            if (channelName == null) {
                synchronized (globalListeners) {
                    globalListeners.add(listener);
                }
            } else {
                synchronized (channels) {
                    ChannelData channel = getChannelData(channelName);
                    channel.listeners.add(listener);
                }
            }
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                                   ResponseCallback<Void> callback,
                                                   ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            if (channelName == null) {
                synchronized (globalListeners) {
                    globalListeners.remove(listener);
                }
            } else {
                synchronized (channels) {
                    ChannelData channel = getChannelData(channelName);
                    channel.listeners.remove(listener);
                }
            }
            return null;
        }, callback, errorCallback);
    }

    private static class ChannelData {

        private final List<MessageInfo> messages = new ArrayList<>();
        private final List<MessageListener> listeners = new ArrayList<>();

    }

}
