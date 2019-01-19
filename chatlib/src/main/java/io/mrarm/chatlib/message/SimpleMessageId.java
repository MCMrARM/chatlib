package io.mrarm.chatlib.message;

import io.mrarm.chatlib.dto.MessageId;

public class SimpleMessageId implements MessageId {

    public static final MessageId.Parser PARSER = new Parser();

    private final int index;

    public SimpleMessageId(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return String.valueOf(index);
    }

    public static class Parser implements MessageId.Parser {

        @Override
        public MessageId parse(String str) {
            return SimpleMessageId.parse(str);
        }

    }

    public static SimpleMessageId parse(String str) {
        return new SimpleMessageId(Integer.parseInt(str));
    }

}
