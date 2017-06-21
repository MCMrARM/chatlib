package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;

import java.util.Map;

public interface Capability extends CommandHandler {

    String[] getNames();

    boolean supportsCapability(CapabilityEntryPair capability);

    void processMessage(MessageInfo.Builder message, Map<String, String> tags);

}
