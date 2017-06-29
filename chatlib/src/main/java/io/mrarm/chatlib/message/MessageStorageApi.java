package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;

import java.util.concurrent.Future;

public interface MessageStorageApi {

    Future<MessageList> getMessages(String channelName, int count, MessageListAfterIdentifier after,
                                    ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback);

    MessageListAfterIdentifier getMessageListAfterIdentifier(String channelName, int count,
                                                             MessageListAfterIdentifier after);

    Future<Void> subscribeChannelMessages(String channelName, MessageListener listener, ResponseCallback<Void> callback,
                                          ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                            ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

}
