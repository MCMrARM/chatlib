package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.*;
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
            MessageId msgId;
            synchronized (channels) {
                ChannelData channel = getChannelData(channelName);
                msgId = new SimpleMessageId(channel.messages.size());
                channel.messages.add(message);
                for (MessageListener listener : channel.listeners)
                    listener.onMessage(channelName, message, msgId);
            }
            synchronized (globalListeners) {
                for (MessageListener listener : globalListeners)
                    listener.onMessage(channelName, message, msgId);
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
                int start;
                int end = data.messages.size();
                if (after != null && after instanceof SimpleMessageListAfterIdentifier) {
                    end = ((SimpleMessageListAfterIdentifier) after).getIndex();
                    start = Math.max(end - count, 0);
                } else if (after != null && after instanceof SimpleMessageListBeforeIdentifier) {
                    start = ((SimpleMessageListBeforeIdentifier) after).getIndex();
                    end = Math.min(end + count, ret.size());
                } else {
                    start = Math.max(end - count, 0);
                }
                if (filter == null) {
                    for (int i = start; i < end; i++)
                        ret.add(data.messages.get(i));
                } else if (after instanceof SimpleMessageListBeforeIdentifier) {
                    ret = new ArrayList<>();
                    int n = count;
                    int msgCnt = data.messages.size();
                    end = start;
                    for ( ; end < msgCnt; ++end) {
                        if (!isMessageIncludedInFilter(data.messages.get(end), filter))
                            continue;
                        ret.add(data.messages.get(end));
                        if (--n == 0) {
                            ++end;
                            break;
                        }
                    }
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
                return new MessageList(ret, new SimpleMessageListBeforeIdentifier(end),
                        new SimpleMessageListAfterIdentifier(start));
            }
        }, callback, errorCallback);
    }

    @Override
    public Future<MessageList> getMessagesNear(String channelName, MessageId messageId, MessageFilterOptions filter,
                                               ResponseCallback<MessageList> callback,
                                               ResponseErrorCallback errorCallback) {
        return SimpleRequestExecutor.run(() -> {
            synchronized (channels) {
                ChannelData data = getChannelData(channelName);
                if (!(messageId instanceof SimpleMessageId))
                    throw new RuntimeException("messageId not a SimpleMessageId");
                int i = ((SimpleMessageId) messageId).getIndex();
                int start, end;
                List<MessageInfo> ret = new ArrayList<>();
                int n = 50;
                for (start = i - 1; start >= 0; --start) {
                    if (!isMessageIncludedInFilter(data.messages.get(start), filter))
                        continue;
                    ret.add(data.messages.get(start));
                    if (--n == 0)
                        break;
                }
                Collections.reverse(ret);
                int msgCnt = data.messages.size();
                n = 50;
                for (end = i; end < msgCnt; ++end) {
                    if (!isMessageIncludedInFilter(data.messages.get(end), filter))
                        continue;
                    ret.add(data.messages.get(end));
                    if (--n == 0) {
                        ++end;
                        break;
                    }
                }

                return new MessageList(ret, new SimpleMessageListBeforeIdentifier(end),
                        new SimpleMessageListAfterIdentifier(start));
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
