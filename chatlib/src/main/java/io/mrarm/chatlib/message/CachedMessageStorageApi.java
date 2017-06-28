package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
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
            channel.sourcePreId = null;
            channel.sourceAfterId = null; // TODO: this will break more stuff
        }
    }

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageListAfterIdentifier after, ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback) {
        if (after != null && !(after instanceof CachedMessageListAfterIdentifier))
            return super.getMessages(channelName, count, after, callback, errorCallback);
        synchronized (channels) {
            if (!channels.containsKey(channelName))
                channels.put(channelName, new ChannelCacheData());
            ChannelCacheData channel = channels.get(channelName);
            List<MessageInfo> ret = new ArrayList<>();
            CachedMessageListAfterIdentifier cafter = (CachedMessageListAfterIdentifier) after;
            int end = (after != null ? cafter.getIndex() - channel.messageIndexOff + cafter.offset : channel.messages.size());
            if (end < 0) { // we don't have that data anymore
                SettableFuture<MessageList> retVal = new SettableFuture<>();
                super.getMessageListAfterIdentifier(channelName, cafter.getIndex(), cafter.sourcePreId, (MessageListAfterIdentifier afterIdentifier) -> {
                    super.getMessages(channelName, count, afterIdentifier, retVal::set, retVal::setExecutionException);
                }, retVal::setExecutionException);
                return retVal;
            }

            int start = end - count;
            int cstart = Math.max(end, 0);
            for (int i = cstart; i < end; i++)
                ret.add(channel.messages.get(i));

            if (start < 0) {
                int requestCount = count - ret.size();
                int missingToFillCache = channel.messages.size() - cacheMessageCount;
                SettableFuture<MessageList> retVal = new SettableFuture<>();
                MessageListAfterIdentifier oldAfterId = channel.sourceAfterId;
                super.getMessages(channelName, count, channel.sourceAfterId, (MessageList list) -> {
                    if (requestCount != missingToFillCache) { // doesn't exactly fill cache
                        super.getMessageListAfterIdentifier(channelName, missingToFillCache, oldAfterId, (MessageListAfterIdentifier ident) -> {
                            appendChannelMessageCache(channel, oldAfterId, list, missingToFillCache, ident);
                        }, null);
                    } else { // exactly fills cache item count
                        appendChannelMessageCache(channel, oldAfterId, list, requestCount, list.getAfterIdentifier());
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

    private void appendChannelMessageCache(ChannelCacheData channel, MessageListAfterIdentifier oldAfterId,
                                           MessageList list, int appendItems, MessageListAfterIdentifier newAfterId) {
        synchronized (channels) {
            if (channel.sourceAfterId != oldAfterId || channel.messages.size() + appendItems != cacheMessageCount) // make sure the data did not change
                return;
            int actualMessagesSize = list.getMessages().size();
            channel.messages.addAll(list.getMessages().subList(actualMessagesSize - appendItems, actualMessagesSize));
            channel.messageIndexOff += appendItems;
            channel.sourceAfterId = newAfterId;
        }
    }

    @Override
    public Future<MessageListAfterIdentifier> getMessageListAfterIdentifier(String channelName, int count, MessageListAfterIdentifier after, ResponseCallback<MessageListAfterIdentifier> callback, ResponseErrorCallback errorCallback) {
        if (after != null && !(after instanceof CachedMessageListAfterIdentifier))
            return super.getMessageListAfterIdentifier(channelName, count, after, callback, errorCallback);
        synchronized (channels) {
            if (!channels.containsKey(channelName))
                return super.getMessageListAfterIdentifier(channelName, count, after, callback, errorCallback);
            ChannelCacheData channel = channels.get(channelName);
            CachedMessageListAfterIdentifier cafter = (CachedMessageListAfterIdentifier) after;
            int end = (after != null ? cafter.getIndex() - channel.messageIndexOff + cafter.offset : channel.messages.size());
            int start = end - count;
            if (start < 0)
                return super.getMessageListAfterIdentifier(channelName, cafter.getIndex() + count, cafter.sourcePreId, callback, errorCallback);
            return SimpleRequestExecutor.run(() -> new CachedMessageListAfterIdentifier(start, channel.messageIndexOff, channel.sourcePreId), callback, errorCallback);
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