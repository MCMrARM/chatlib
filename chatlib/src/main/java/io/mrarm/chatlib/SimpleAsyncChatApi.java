package io.mrarm.chatlib;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SimpleAsyncChatApi implements ChatApi {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    protected <T> void queue(final Callable<T> task, final ResponseCallback<T> callback,
                             final ResponseErrorCallback errorCallback) {
        executor.submit(() -> {
            T ret;
            try {
                ret = task.call();
            } catch (Exception ex) {
                if (ex instanceof ChatApiException) {
                    if (errorCallback != null)
                        errorCallback.onError((ChatApiException) ex);
                    return;
                }
                throw new RuntimeException(ex);
            }
            callback.onResponse(ret);
        });
    }

}
