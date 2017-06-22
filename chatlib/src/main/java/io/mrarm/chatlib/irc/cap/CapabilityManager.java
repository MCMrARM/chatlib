package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityManager {

    private ServerConnectionData connection;
    private Map<String, List<Capability>> supportedCapabilities = new HashMap<>();
    private List<Capability> enabledCapabilities = new ArrayList<>();
    private boolean negotiationFinished = false;

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
        for (Capability cap : capabilities)
            connection.getCommandHandlerList().registerHandler(cap);
    }

    public void onServerCapabilityList(List<CapabilityEntryPair> capabilities) {
        List<String> requestedCapabilities = new ArrayList<>();
        for (CapabilityEntryPair capability : capabilities) {
            if (supportedCapabilities.containsKey(capability.getName())) {
                for (Capability s : supportedCapabilities.get(capability.getName())) {
                    if (!s.supportsCapability(capability))
                        continue;
                    requestedCapabilities.add(capability.getName());
                    break;
                }
            }
        }

        connection.getApi().requestCapabilities(requestedCapabilities);
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

        if (!negotiationFinished) {
            connection.getApi().endCapabilityNegotiation();
            negotiationFinished = true;
        }
    }

}
