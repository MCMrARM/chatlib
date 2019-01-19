package io.mrarm.chatlib.message;

import io.mrarm.chatlib.dto.MessageId;

public class SimpleMessageId implements MessageId {

    private final int index;

    public SimpleMessageId(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
