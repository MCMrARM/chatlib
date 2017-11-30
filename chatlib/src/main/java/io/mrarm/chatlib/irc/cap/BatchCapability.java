package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.dto.BatchInfo;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.*;

public class BatchCapability extends Capability {

    private final Map<String, BatchInfo> batches = new HashMap<>();
    private final Map<String, List<BatchListener>> batchListeners = new HashMap<>();

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
        String name = CommandHandler.getParamWithCheck(params, 0);
        if (name.startsWith("+")) {
            name = name.substring(1);
            BatchInfo batch = new BatchInfo(UUID.randomUUID(), CommandHandler.getParamWithCheck(params, 1),
                    params.subList(1, params.size()).toArray(new String[params.size() - 1]), getBatchForTags(tags));
            batches.put(name, batch);
            if (batchListeners.containsKey(batch.getType())) {
                for (BatchListener listener : batchListeners.get(batch.getType()))
                    listener.onBatchStart(connection, batch);
            }
        } else if (name.startsWith("-")) {
            name = name.substring(1);
            BatchInfo batch = batches.remove(name);
            if (batch != null && batchListeners.containsKey(batch.getType())) {
                for (BatchListener listener : batchListeners.get(batch.getType()))
                    listener.onBatchEnd(connection, batch);
            }
        }
    }

    public void addBatchListener(String batchName, BatchListener listener) {
        if (!batchListeners.containsKey(batchName))
            batchListeners.put(batchName, new ArrayList<>());
        batchListeners.get(batchName).add(listener);
    }

    public void removeBatchListener(String batchName, BatchListener listener) {
        if (batchListeners.containsKey(batchName))
            batchListeners.get(batchName).remove(listener);
    }

    public interface BatchListener {

        void onBatchStart(ServerConnectionData connection, BatchInfo batch);

        void onBatchEnd(ServerConnectionData connection, BatchInfo batch);

    }

}
