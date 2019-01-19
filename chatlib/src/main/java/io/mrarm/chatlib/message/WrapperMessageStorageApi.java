package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageFilterOptions;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;

import java.util.concurrent.Future;

public class WrapperMessageStorageApi implements MessageStorageApi {

    private MessageStorageApi wrapped;

    public WrapperMessageStorageApi(MessageStorageApi wrapped) {
        this.wrapped = wrapped;
    }

    public MessageStorageApi getWrappedApi() {
        return wrapped;
    }

    @Override
    public Future<MessageList> getMessages(String channelName, int count, MessageFilterOptions options, MessageListAfterIdentifier after, ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback) {
        return wrapped.getMessages(channelName, count, options, after, callback, errorCallback);
    }

    @Override
    public Future<Void> subscribeChannelMessages(String channelName, MessageListener listener, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return wrapped.subscribeChannelMessages(channelName, listener, callback, errorCallback);
    }

    @Override
    public Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return wrapped.unsubscribeChannelMessages(channelName, listener, callback, errorCallback);
    }

}
