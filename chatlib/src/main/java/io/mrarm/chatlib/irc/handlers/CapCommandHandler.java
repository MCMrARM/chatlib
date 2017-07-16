package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.irc.cap.CapabilityEntryPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CapCommandHandler implements CommandHandler {

    private List<CapabilityEntryPair> lsEntries;
    private List<String> ackEntries;

    @Override
    public String[] getHandledCommands() {
        return new String[] { "CAP" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        int baseSubcmdI = 1;
        String subcmd = params.get(baseSubcmdI);
        boolean expectMore = false;
        if (subcmd.equals("*")) {
            expectMore = true;
            subcmd = params.get(++baseSubcmdI);
        }
        if (subcmd.equals("LS")) {
            if (lsEntries == null)
                lsEntries = new ArrayList<>();
            for (String s : params.get(baseSubcmdI + 1).split(" "))
                lsEntries.add(new CapabilityEntryPair(s));
            if (!expectMore) {
                connection.getCapabilityManager().onServerCapabilityList(lsEntries);
                lsEntries = null;
            }
        } else if (subcmd.equals("ACK")) {
            if (ackEntries == null)
                ackEntries = new ArrayList<>();
            for (String s : params.get(baseSubcmdI + 1).split(" "))
                ackEntries.add(s);
            if (!expectMore) {
                connection.getCapabilityManager().onCapabilitiesAck(ackEntries);
                ackEntries = null;
            }
        } else if (subcmd.equals("NEW")) {
            List<CapabilityEntryPair> entries = new ArrayList<>();
            for (String s : params.get(baseSubcmdI + 1).split(" "))
                entries.add(new CapabilityEntryPair(s));
            connection.getCapabilityManager().onNewServerCapabilitiesAvailable(entries);
        } else if (subcmd.equals("DEL")) {
            List<String> entries = new ArrayList<>();
            Collections.addAll(entries, params.get(baseSubcmdI + 1).split(" "));
            connection.getCapabilityManager().onServerCapabilitiesRemoved(entries);
        } else {
            throw new InvalidMessageException("Unknown subcommand: " + subcmd);
        }
    }

}
