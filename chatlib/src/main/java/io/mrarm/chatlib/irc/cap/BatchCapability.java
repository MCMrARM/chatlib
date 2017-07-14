package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.dto.BatchInfo;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BatchCapability extends Capability {

    private Map<String, BatchInfo> batches = new HashMap<>();

    @Override
    public String[] getNames() {
        return new String[] { "batch", "znc.in/batch" };
    }

    @Override
    public String[] getHandledCommands() {
        return new String[] { "BATCH" };
    }

    private BatchInfo getBatchForTags(Map<String, String> tags) {
        if (tags.containsKey("batch")) {
            String batchName = tags.get("batch");
            if (batches.containsKey(batchName))
                return batches.get(batchName);
        }
        return null;
    }

    @Override
    public void processMessage(MessageInfo.Builder message, Map<String, String> tags) {
        BatchInfo batch = getBatchForTags(tags);
        if (batch != null)
            message.setBatch(batch);
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        String name = params.get(0);
        if (name.startsWith("+")) {
            name = name.substring(1);
            batches.put(name, new BatchInfo(UUID.randomUUID(), params.get(1),
                    params.subList(1, params.size()).toArray(new String[params.size() - 1]), getBatchForTags(tags)));
        } else if (name.startsWith("-")) {
            batches.remove(name.substring(1));
        }
    }
}
