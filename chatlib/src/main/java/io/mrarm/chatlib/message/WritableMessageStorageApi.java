package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;

import java.util.concurrent.Future;

public interface WritableMessageStorageApi extends MessageStorageApi {

    Future<Void> addMessage(String channelName, MessageInfo message, ResponseCallback<Void> callback,
                            ResponseErrorCallback errorCallback);


}
