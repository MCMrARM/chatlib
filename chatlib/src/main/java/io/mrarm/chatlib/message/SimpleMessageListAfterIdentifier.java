package io.mrarm.chatlib.message;

import io.mrarm.chatlib.dto.MessageListAfterIdentifier;

public class SimpleMessageListAfterIdentifier implements MessageListAfterIdentifier {

    private final int index;

    public SimpleMessageListAfterIdentifier(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
