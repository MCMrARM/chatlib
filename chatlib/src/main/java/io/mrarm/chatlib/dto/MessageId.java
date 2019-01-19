package io.mrarm.chatlib.dto;

public interface MessageId {

    // NOTE: must be serializable using toString

    interface Parser {

        MessageId parse(String str);

    }

}
