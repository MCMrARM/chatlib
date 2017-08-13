package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.ChatApiException;

public class NumericReplyException extends ChatApiException {

    private int errorCommandId;
    private String errorMessage;

    public NumericReplyException(int errorCommandId, String errorMessage) {
        super(errorMessage);
    }

    public int getErrorCommandId() {
        return errorCommandId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}