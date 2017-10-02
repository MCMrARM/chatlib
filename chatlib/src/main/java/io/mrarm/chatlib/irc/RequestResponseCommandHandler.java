package io.mrarm.chatlib.irc;

import java.util.*;

public abstract class RequestResponseCommandHandler<RequestIdentifier, ResponseType> implements CommandDisconnectHandler,
        ErrorCommandHandler.ErrorCallback {

    private final ErrorCommandHandler errorCommandHandler;
    private final Map<RequestIdentifier, Queue<CallbackData<RequestIdentifier, ResponseType>>> callbacks = new HashMap<>();
    private final int[] handledErrors;
    private final boolean sharedResponseMode;

    public RequestResponseCommandHandler(ErrorCommandHandler handler, boolean sharedResponseMode) {
        errorCommandHandler = handler;
        handledErrors = getHandledErrors();
        this.sharedResponseMode = sharedResponseMode;
    }

    public abstract int[] getHandledErrors();

    public boolean onRequested(RequestIdentifier i, Callback<ResponseType> callback, ErrorCallback<RequestIdentifier> errorCallback) {
        synchronized (callbacks) {
            boolean existed = true;
            if (!callbacks.containsKey(i)) {
                callbacks.put(i, new ArrayDeque<>());
                existed = false;
            }
            callbacks.get(i).add(new CallbackData<>(callback, errorCallback));
            if (!sharedResponseMode || !existed)
                errorCommandHandler.addErrorCallback(handledErrors, this);
            return !sharedResponseMode || !existed;
        }
    }

    protected boolean onError(RequestIdentifier i, int commandId, String error, boolean unqueueErrorCallback) {
        synchronized (callbacks) {
            if (callbacks.containsKey(i)) {
                Queue<CallbackData<RequestIdentifier, ResponseType>> l = callbacks.get(i);
                if (unqueueErrorCallback)
                    errorCommandHandler.cancelErrorCallback(handledErrors, this);
                if (sharedResponseMode) {
                    callbacks.remove(i);
                    for (CallbackData<RequestIdentifier, ResponseType> it : l) {
                        if (it.errorCallback != null)
                            it.errorCallback.onError(i, commandId, error);
                    }
                } else {
                    ErrorCallback<RequestIdentifier> cb = l.remove().errorCallback;
                    if (l.size() == 0)
                        callbacks.remove(i);
                    if (cb != null)
                        cb.onError(i, commandId, error);
                }
                return true;
            }
        }
        return false;
    }

    protected void onResponse(RequestIdentifier i, ResponseType resp) {
        synchronized (callbacks) {
            if (callbacks.containsKey(i)) {
                Queue<CallbackData<RequestIdentifier, ResponseType>> l = callbacks.get(i);
                errorCommandHandler.cancelErrorCallback(handledErrors, this);
                if (sharedResponseMode) {
                    callbacks.remove(i);
                    for (CallbackData<RequestIdentifier, ResponseType> it : l) {
                        if (it.callback != null)
                            it.callback.onResponse(resp);
                    }
                } else {
                    Callback<ResponseType> ret = l.remove().callback;
                    if (l.size() == 0)
                        callbacks.remove(i);
                    if (ret != null)
                        ret.onResponse(resp);
                }
            }
        }
    }

    protected void onCancelled(RequestIdentifier i) {
        synchronized (callbacks) {
            if (callbacks.containsKey(i)) {
                Queue<CallbackData<RequestIdentifier, ResponseType>> l = callbacks.get(i);
                errorCommandHandler.cancelErrorCallback(handledErrors, this);
                if (sharedResponseMode) {
                    callbacks.remove(i);
                } else {
                    l.remove();
                    if (l.size() == 0)
                        callbacks.remove(i);
                }
            }
        }
    }

    @Override
    public void onDisconnected() {
        synchronized (callbacks) {
            for (Map.Entry<RequestIdentifier, Queue<CallbackData<RequestIdentifier, ResponseType>>> entry : callbacks.entrySet()) {
                if (sharedResponseMode)
                    errorCommandHandler.cancelErrorCallback(handledErrors, this);
                for (CallbackData<RequestIdentifier, ResponseType> it : entry.getValue()) {
                    if (!sharedResponseMode)
                        errorCommandHandler.cancelErrorCallback(handledErrors, this);
                    if (it.errorCallback != null)
                        it.errorCallback.onError(entry.getKey(), -1, "Disconnected from server");
                }
            }
            callbacks.clear();
        }
    }

    public boolean hasRequest(RequestIdentifier i) {
        synchronized (callbacks) {
            return callbacks.containsKey(i);
        }
    }

    public void reset() {
        synchronized (callbacks) {
            callbacks.clear();
        }
    }

    public interface Callback<ResponseType> {

        void onResponse(ResponseType type);

    }

    public interface ErrorCallback<RequestIdentifier> {

        void onError(RequestIdentifier i, int errorCommandId, String errorMessage);

    }

    private static class CallbackData<RequestIdentifier, ResponseType> {
        Callback<ResponseType> callback;
        ErrorCallback<RequestIdentifier> errorCallback;

        CallbackData(Callback<ResponseType> cb, ErrorCallback<RequestIdentifier> e) {
            this.callback = cb;
            this.errorCallback = e;
        }
    }

}
