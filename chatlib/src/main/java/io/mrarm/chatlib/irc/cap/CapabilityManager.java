package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.irc.ServerConnectionData;

import java.io.IOException;
import java.util.*;

public class CapabilityManager {

    private ServerConnectionData connection;
    private Map<String, List<Capability>> supportedCapabilities = new HashMap<>();
    private List<Capability> enabledCapabilities = new ArrayList<>();
    private Set<Integer> negotationFinishLocks = new HashSet<>();
    private int nextNegotiationFinishLockId = 0;
    private boolean negotiationFinished = false;
    private boolean negotiationFinishWaitingForLocks = false;

    public CapabilityManager(ServerConnectionData connection) {
        this.connection = connection;
    }

    public void reset() {
        setEnabledCapabilities(new ArrayList<>());
        negotiationFinished = false;
    }

    public void addDefaultCapabilities() {
        registerCapability(new BatchCapability());
        registerCapability(new ServerTimeCapability());
        registerCapability(new MultiPrefixCapability());
    }

    public List<Capability> getEnabledCapabilities() {
        return enabledCapabilities;
    }

    public void registerCapability(Capability capability) {
        for (String name : capability.getNames()) {
            if (!supportedCapabilities.containsKey(name))
                supportedCapabilities.put(name, new ArrayList<>());
            supportedCapabilities.get(name).add(capability);
        }
    }

    private void setEnabledCapabilities(List<Capability> capabilities) {
        for (Capability cap : enabledCapabilities)
            connection.getCommandHandlerList().unregisterHandler(cap);
        enabledCapabilities = capabilities;
        for (Capability cap : capabilities) {
            connection.getCommandHandlerList().registerHandler(cap);
            cap.onEnabled(connection);
        }
    }

    public void onServerCapabilityList(List<CapabilityEntryPair> capabilities) {
        List<String> requestedCapabilities = new ArrayList<>();
        for (CapabilityEntryPair capability : capabilities) {
            if (supportedCapabilities.containsKey(capability.getName())) {
                for (Capability s : supportedCapabilities.get(capability.getName())) {
                    if (!s.shouldEnableCapability(connection, capability))
                        continue;
                    requestedCapabilities.add(capability.getName());
                    break;
                }
            }
        }

        if (requestedCapabilities.size() > 0) {
            requestCapabilities(requestedCapabilities);
        } else if (!negotiationFinished) {
            endCapabilityNegotiation();
        }
    }

    public void onCapabilitiesAck(List<String> capabilities) {
        List<Capability> newCapabilities = new ArrayList<>();
        for (String capability : capabilities) {
            if (supportedCapabilities.containsKey(capability)) {
                Capability s = supportedCapabilities.get(capability).get(0);
                if (s != null)
                    newCapabilities.add(s);
            }
        }
        setEnabledCapabilities(newCapabilities);

        if (!negotiationFinished)
            endCapabilityNegotiation();
    }

    public int lockNegotationFinish() {
        int ret = nextNegotiationFinishLockId++;
        negotationFinishLocks.add(ret);
        return ret;
    }

    public void removeNegotationFinishLock(int id) {
        negotationFinishLocks.remove(id);
        if (negotationFinishLocks.size() == 0 && negotiationFinishWaitingForLocks && !negotiationFinished)
            endCapabilityNegotiation();
    }

    private void requestCapabilities(List<String> capabilities) {
        // TODO: Somehow handle a situation where the resulting string is larger than the maximal allowed message
        // length (specs don't really mention what should be done in this case ?)
        StringBuilder capsBuilder = new StringBuilder();
        boolean f = true;
        for (String cap : capabilities) {
            if (f)
                f = false;
            else
                capsBuilder.append(' ');
            capsBuilder.append(cap);
        }
        try {
            connection.getApi().sendCommand("CAP", true, "REQ", capsBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void endCapabilityNegotiation() {
        if (negotationFinishLocks.size() > 0) {
            negotiationFinishWaitingForLocks = true;
            return;
        }
        negotiationFinished = true;
        negotiationFinishWaitingForLocks = false;
        try {
            connection.getApi().sendCommand("CAP", false, "END");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
