package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.irc.cap.CapabilityEntryPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CapCommandHandler implements CommandDisconnectHandler {

    private List<CapabilityEntryPair> lsEntries;
    private List<String> ackEntries;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "CAP" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        int baseSubcmdI = 1;
        String subcmd = CommandHandler.getParamWithCheck(params, baseSubcmdI);
        boolean expectMore = false;
        if (subcmd.equals("*")) {
            expectMore = true;
            subcmd = CommandHandler.getParamWithCheck(params, ++baseSubcmdI);
        }
        if (subcmd.equals("LS")) {
            if (lsEntries == null)
                lsEntries = new ArrayList<>();
            for (String s : CommandHandler.getParamWithCheck(params, baseSubcmdI + 1).split(" "))
                lsEntries.add(new CapabilityEntryPair(s));
            if (!expectMore) {
                connection.getCapabilityManager().onServerCapabilityList(lsEntries);
                lsEntries = null;
            }
        } else if (subcmd.equals("ACK")) {
            if (ackEntries == null)
                ackEntries = new ArrayList<>();
            for (String s : CommandHandler.getParamWithCheck(params, baseSubcmdI + 1).split(" "))
                ackEntries.add(s);
            if (!expectMore) {
                connection.getCapabilityManager().onCapabilitiesAck(ackEntries);
                ackEntries = null;
            }
        } else if (subcmd.equals("NEW")) {
            List<CapabilityEntryPair> entries = new ArrayList<>();
            for (String s : CommandHandler.getParamWithCheck(params, baseSubcmdI + 1).split(" "))
                entries.add(new CapabilityEntryPair(s));
            connection.getCapabilityManager().onNewServerCapabilitiesAvailable(entries);
        } else if (subcmd.equals("DEL")) {
            List<String> entries = new ArrayList<>();
            Collections.addAll(entries, CommandHandler.getParamWithCheck(params, baseSubcmdI + 1).split(" "));
            connection.getCapabilityManager().onServerCapabilitiesRemoved(entries);
        } else {
            throw new InvalidMessageException("Unknown subcommand: " + subcmd);
        }
    }

    @Override
    public void onDisconnected() {
        if (lsEntries != null)
            lsEntries = null;
        if (ackEntries != null)
            ackEntries = null;
    }
}
