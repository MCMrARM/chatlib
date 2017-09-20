package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageFilterOptions;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;
import io.mrarm.chatlib.util.SettableFuture;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class CachedMessageStorageApi extends WrapperMessageStorageApi implements MessageListener {

    protected final Map<String, ChannelCacheData> channels = new HashMap<>();
    protected final int cacheMessageCount;

    public CachedMessageStorageApi(MessageStorageApi api, int cacheMessageCount) {
        super(api);
        this.cacheMessageCount = cacheMessageCount;
        try {
            api.subscribeChannelMessages(null, this, null, null).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(String channelName, MessageInfo message) {
        synchronized (channels) {
            if (!channels.containsKey(channelName))
                channels.put(channelName, new ChannelCacheData());
            ChannelCacheData channel = channels.get(channelName);
            channel.messageIndexOff++;
            channel.messages.add(message);
            channel.sourcePreId = getWrappedApi().getMessageListAfterIdentifier(channelName, 0, null, null);
            channel.sourceAfterId = getWrappedApi().getMessageListAfterIdentifier(channelName, channel.messageIndexOff, null, channel.sourcePreId);
        }
    }

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageFilterOptions options,
                                           MessageListAfterIdentifier after, ResponseCallback<MessageList> callback,
                                           ResponseErrorCallback errorCallback) {
        if ((after != null && !(after instanceof CachedMessageListAfterIdentifier)) || options != null)
            return super.getMessages(channelName, count, options, after, callback, errorCallback);
        synchronized (channels) {
            if (!channels.containsKey(channelName))
                channels.put(channelName, new ChannelCacheData());
            ChannelCacheData channel = channels.get(channelName);
            List<MessageInfo> ret = new ArrayList<>();
            CachedMessageListAfterIdentifier cafter = (CachedMessageListAfterIdentifier) after;
            int end = (after != null ? cafter.getIndex() - channel.messageIndexOff + cafter.offset : channel.messages.size());
            if (end < 0) { // we don't have that data anymore
                MessageListAfterIdentifier afterIdentifier = super.getMessageListAfterIdentifier(channelName, cafter.getIndex(), null, cafter.sourcePreId);
                return super.getMessages(channelName, count, null, afterIdentifier, callback, errorCallback);
            }

            int start = end - count;
            int cstart = Math.max(end, 0);
            for (int i = cstart; i < end; i++)
                ret.add(channel.messages.get(i));

            if (start < 0) {
                SettableFuture<MessageList> retVal = new SettableFuture<>();
                MessageListAfterIdentifier oldAfterId = channel.sourceAfterId;
                super.getMessages(channelName, count, null, channel.sourceAfterId, (MessageList list) -> {
                    synchronized (channels) {
                        if (channel.sourceAfterId == oldAfterId) {
                            int missingToFillCache = cacheMessageCount - channel.messages.size();
                            int actualMessagesSize = list.getMessages().size();
                            channel.messages.addAll(list.getMessages().subList(actualMessagesSize - missingToFillCache, actualMessagesSize));
                            if (missingToFillCache == actualMessagesSize)
                                channel.sourceAfterId = list.getAfterIdentifier();
                            else
                                channel.sourceAfterId = super.getMessageListAfterIdentifier(channelName, missingToFillCache, null, channel.sourceAfterId);
                        }
                    }
                    ret.addAll(0, list.getMessages());
                    retVal.set(new MessageList(ret, list.getAfterIdentifier()));
                }, retVal::setExecutionException);
                return retVal;
            } else {
                MessageListAfterIdentifier afterId;
                if (start == 0)
                    afterId = channel.sourceAfterId;
                else
                    afterId = new CachedMessageListAfterIdentifier(start, channel.messageIndexOff, channel.sourcePreId);
                return SimpleRequestExecutor.run(() -> new MessageList(ret, afterId), callback, errorCallback);
            }
        }
    }

    @Override
    public MessageListAfterIdentifier getMessageListAfterIdentifier(String channelName, int count, MessageFilterOptions options, MessageListAfterIdentifier after) {
        if ((after != null && !(after instanceof CachedMessageListAfterIdentifier)) || options != null)
            return super.getMessageListAfterIdentifier(channelName, count, options, after);
        synchronized (channels) {
            if (!channels.containsKey(channelName))
                return super.getMessageListAfterIdentifier(channelName, count, null, after);
            ChannelCacheData channel = channels.get(channelName);
            CachedMessageListAfterIdentifier cafter = (CachedMessageListAfterIdentifier) after;
            int end = (after != null ? cafter.getIndex() - channel.messageIndexOff + cafter.offset : channel.messages.size());
            int start = end - count;
            if (start < 0)
                return super.getMessageListAfterIdentifier(channelName, cafter.getIndex() + count, null, cafter.sourcePreId);
            return new CachedMessageListAfterIdentifier(start, channel.messageIndexOff, channel.sourcePreId);
        }
    }

    protected static class CachedMessageListAfterIdentifier extends SimpleMessageListAfterIdentifier {

        int offset;
        MessageListAfterIdentifier sourcePreId;

        CachedMessageListAfterIdentifier(int index, int offset, MessageListAfterIdentifier preId) {
            super(index);
            this.offset = offset;
            sourcePreId = preId;
        }

    }

    protected static class ChannelCacheData {

        public List<MessageInfo> messages;
        public int messageIndexOff;
        public MessageListAfterIdentifier sourcePreId;
        public MessageListAfterIdentifier sourceAfterId;

    }

}