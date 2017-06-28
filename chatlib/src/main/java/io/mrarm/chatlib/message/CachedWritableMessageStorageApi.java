package io.mrarm.chatlib.message;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;

import java.util.concurrent.Future;

public class CachedWritableMessageStorageApi extends CachedMessageStorageApi implements WritableMessageStorageApi {

    private WritableMessageStorageApi api;

    public CachedWritableMessageStorageApi(WritableMessageStorageApi api, int cacheMessageCount) {
        super(api, cacheMessageCount);
        this.api = api;
    }

    @Override
    public Future<Void> addMessage(String channelName, MessageInfo message, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return api.addMessage(channelName, message, callback, errorCallback);
    }
}
