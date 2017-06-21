package io.mrarm.chatlib.dto;

import java.util.UUID;

public class BatchInfo {

    private UUID uuid;
    private String type;
    private String[] params;
    private BatchInfo parentBatch;

    public BatchInfo(UUID uuid, String type, String[] params, BatchInfo parent) {
        this.uuid = uuid;
        this.type = type;
        this.params = params;
        this.parentBatch = parent;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public String[] getParams() {
        return params;
    }

    public BatchInfo getParentBatch() {
        return parentBatch;
    }

}
