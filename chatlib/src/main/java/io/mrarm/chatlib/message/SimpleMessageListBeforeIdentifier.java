package io.mrarm.chatlib.message;

import io.mrarm.chatlib.dto.MessageListAfterIdentifier;

public class SimpleMessageListBeforeIdentifier implements MessageListAfterIdentifier {

    private final int index;

    public SimpleMessageListBeforeIdentifier(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
