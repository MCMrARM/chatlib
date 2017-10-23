package io.mrarm.chatlib.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SettableFuture<V> implements Future<V> {

    private V value;
    private boolean valueSet = false;
    private Exception exception;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        synchronized (this) {
            return valueSet;
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized (this) {
            while (!valueSet) {
                wait();
            }
            if (exception != null)
                throw new ExecutionException(exception);
            return value;
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (this) {
            while (!valueSet) {
                wait(unit.toMillis(timeout), (int) (unit.toNanos(timeout) % 1000000L));
            }
            return value;
        }
    }

    public void set(V value) {
        synchronized (this) {
            this.value = value;
            valueSet = true;
            notifyAll();
        }
    }

    public void setExecutionException(Exception exception) {
        synchronized (this) {
            this.exception = exception;
            valueSet = true;
            notifyAll();
        }
    }

}
