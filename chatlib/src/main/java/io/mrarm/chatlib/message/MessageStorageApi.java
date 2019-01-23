package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageFilterOptions;
import io.mrarm.chatlib.dto.MessageId;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;

import java.util.List;
import java.util.concurrent.Future;

public interface MessageStorageApi {

    MessageId.Parser getMessageIdParser();

    Future<MessageList> getMessages(String channelName, int count, MessageFilterOptions options,
                                    MessageListAfterIdentifier after, ResponseCallback<MessageList> callback,
                                    ResponseErrorCallback errorCallback);

    Future<MessageList> getMessagesNear(String channelName, MessageId messageId, MessageFilterOptions options,
                                        ResponseCallback<MessageList> callback,
                                        ResponseErrorCallback errorCallback);

    Future<Void> deleteMessages(String channelName, List<MessageId> messages, ResponseCallback<Void> callback,
                                ResponseErrorCallback errorCallback);

    Future<Void> subscribeChannelMessages(String channelName, MessageListener listener, ResponseCallback<Void> callback,
                                          ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                            ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

}
