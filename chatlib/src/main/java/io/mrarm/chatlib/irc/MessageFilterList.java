package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.dto.MessageInfo;

import java.util.ArrayList;
import java.util.List;

public class MessageFilterList {

    private final List<MessageFilter> mMessageFilters = new ArrayList<>();

    public void addMessageFilter(MessageFilter filter) {
        synchronized (mMessageFilters) {
            mMessageFilters.add(filter);
        }
    }

    public void removeMessageFilter(MessageFilter filter) {
        synchronized (mMessageFilters) {
            mMessageFilters.remove(filter);
        }
    }

    public boolean filterMessage(ServerConnectionData connection, String channel, MessageInfo message) {
        synchronized (mMessageFilters) {
            for (MessageFilter filter : mMessageFilters) {
                if (!filter.filter(connection, channel, message))
                    return false;
            }
        }
        return true;
    }

}
