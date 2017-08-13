package io.mrarm.chatlib.irc;

import java.util.*;

public abstract class RequestResponseCommandHandler<RequestIdentifier, CallbackType> implements CommandHandler,
        ErrorCommandHandler.ErrorCallback {

    private final ErrorCommandHandler errorCommandHandler;
    private final Map<RequestIdentifier, Queue<CallbackData<RequestIdentifier, CallbackType>>> callbacks = new HashMap<>();
    private final int[] handledErrors;

    public RequestResponseCommandHandler(ErrorCommandHandler handler) {
        errorCommandHandler = handler;
        handledErrors = getHandledErrors();
    }

    public abstract int[] getHandledErrors();

    public void onRequested(RequestIdentifier i, CallbackType callback, ErrorCallback<RequestIdentifier> errorCallback) {
        synchronized (callbacks) {
            if (!callbacks.containsKey(i))
                callbacks.put(i, new ArrayDeque<>());
            callbacks.get(i).add(new CallbackData<>(callback, errorCallback));
            errorCommandHandler.addErrorCallback(handledErrors, this);
        }
    }

    protected boolean onError(RequestIdentifier i, int commandId, String error, boolean unqueueErrorCallback) {
        synchronized (callbacks) {
            if (callbacks.containsKey(i)) {
                Queue<CallbackData<RequestIdentifier, CallbackType>> l = callbacks.get(i);
                ErrorCallback<RequestIdentifier> cb = l.remove().errorCallback;
                if (unqueueErrorCallback)
                    errorCommandHandler.cancelErrorCallback(handledErrors, this);
                if (l.size() == 0)
                    callbacks.remove(i);
                cb.onError(i, commandId, error);
                return true;
            }
        }
        return false;
    }

    protected CallbackType requestResponseCallbacksFor(RequestIdentifier i) {
        synchronized (callbacks) {
            if (callbacks.containsKey(i)) {
                Queue<CallbackData<RequestIdentifier, CallbackType>> l = callbacks.get(i);
                CallbackType ret = l.remove().callback;
                if (l.size() == 0)
                    callbacks.remove(i);
                errorCommandHandler.cancelErrorCallback(handledErrors, this);
                return ret;
            }
            return null;
        }
    }

    public boolean hasRequest(RequestIdentifier i) {
        synchronized (callbacks) {
            return callbacks.containsKey(i);
        }
    }

    public interface ErrorCallback<RequestIdentifier> {

        void onError(RequestIdentifier i, int errorCommandId, String errorMessage);

    }

    private static class CallbackData<RequestIdentifier, CallbackType> {
        CallbackType callback;
        ErrorCallback<RequestIdentifier> errorCallback;

        CallbackData(CallbackType cb, ErrorCallback<RequestIdentifier> e) {
            this.callback = cb;
            this.errorCallback = e;
        }
    }

}
