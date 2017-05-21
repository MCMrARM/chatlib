package io.mrarm.chatlib;

public class ChatApiException extends Exception {

    public ChatApiException(String message) {
        super(message);
    }

    public ChatApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatApiException(Throwable cause) {
        super( cause);
    }

}
