package io.mrarm.chatlib;

public interface ResponseCallback<T> {

    void onResponse(T response);

}
