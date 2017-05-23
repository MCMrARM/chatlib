package io.mrarm.chatlib.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InstantFuture<T> implements Future<T> {

    private final T value;
    private final Throwable throwable;

    public InstantFuture(T value) {
        this.value = value;
        this.throwable = null;
    }

    public InstantFuture(T value, Throwable throwable) {
        this.value = value;
        this.throwable = throwable;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }

    public T get() throws ExecutionException {
        if (throwable != null)
            throw new ExecutionException(throwable);
        return value;
    }

    public T get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }

}
