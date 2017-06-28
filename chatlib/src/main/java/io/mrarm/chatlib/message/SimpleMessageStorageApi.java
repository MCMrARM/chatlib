package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageListAfterIdentifier after,
                                           ResponseCallback<MessageList> callback,
                                           ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (channels) {
                ChannelData data = getChannelData(channelName);
                List<MessageInfo> ret = new ArrayList<>();
                int end = data.messages.size();
                if (after != null && after instanceof SimpleMessageListAfterIdentifier)
                    end = ((SimpleMessageListAfterIdentifier) after).getIndex();
                int start = Math.max(end - count, 0);
                for (int i = start; i < end; i++)
                    ret.add(data.messages.get(i));
                return new MessageList(ret, new SimpleMessageListAfterIdentifier(start));
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<MessageListAfterIdentifier> getMessageListAfterIdentifier(String channelName, int count, MessageListAfterIdentifier after, ResponseCallback<MessageListAfterIdentifier> callback, ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (channels) {
                ChannelData data = getChannelData(channelName);
                int end = data.messages.size();
                if (after != null && after instanceof SimpleMessageListAfterIdentifier)
                    end = ((SimpleMessageListAfterIdentifier) after).getIndex();
                int start = Math.max(end - count, 0);
                return new SimpleMessageListAfterIdentifier(start);
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
