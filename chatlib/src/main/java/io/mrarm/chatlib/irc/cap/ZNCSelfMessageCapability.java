package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.List;
import java.util.Map;

public class ZNCSelfMessageCapability extends Capability {

    @Override
    public String[] getNames() {
        return new String[] { "znc.in/self-message" };
    }

    @Override
    public String[] getHandledCommands() {
        return new String[0];
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
    }

}
