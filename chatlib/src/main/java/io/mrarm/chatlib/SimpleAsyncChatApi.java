package io.mrarm.chatlib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class SimpleAsyncChatApi implements ChatApi {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    protected <T> Future<T> queue(final Callable<T> task, final ResponseCallback<T> callback,
                                  final ResponseErrorCallback errorCallback) {
        return executor.submit(() -> {
            T ret;
            try {
                ret = task.call();
            } catch (Exception ex) {
                if (ex instanceof ChatApiException && errorCallback != null) {
                    errorCallback.onError((ChatApiException) ex);
                    return null;
                }
                throw ex;
            }
            if (callback != null)
                callback.onResponse(ret);
            return ret;
        });
    }

}
