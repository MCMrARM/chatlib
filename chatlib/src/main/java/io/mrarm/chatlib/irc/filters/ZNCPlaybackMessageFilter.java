package io.mrarm.chatlib.irc.filters;

import io.mrarm.chatlib.dto.*;
import io.mrarm.chatlib.irc.MessageFilter;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.irc.cap.BatchCapability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ZNCPlaybackMessageFilter implements MessageFilter, BatchCapability.BatchListener {

    private static final String ZNC_PLAYBACK_BATCH = "znc.in/playback";

    private final static MessageFilterOptions messagesOnlyFilter;
    private final Map<String, List<MessageInfo>> channelData = new HashMap<>();
    private boolean passthru = false;

    public ZNCPlaybackMessageFilter(ServerConnectionData connectionData) {
        connectionData.getCapabilityManager().getCapability(BatchCapability.class).addBatchListener(ZNC_PLAYBACK_BATCH, this);
    }

    @Override
    public boolean filter(ServerConnectionData connection, String channel, MessageInfo messageInfo) {
        if (!passthru && messageInfo.getBatch() != null && messageInfo.getBatch().getType().equals(ZNC_PLAYBACK_BATCH)) {
            // Store the message for processing it later
            if (!channelData.containsKey(channel))
                channelData.put(channel, new ArrayList<>());
            channelData.get(channel).add(messageInfo);
            return false;
        }
        return true;
    }

    @Override
    public void onBatchStart(ServerConnectionData connection, BatchInfo batch) {
        // stub
    }

    private List<MessageInfo> getChannelMessageLog(ServerConnectionData connection, String channel, int count) {
        try {
            MessageList list = connection.getMessageStorageApi().getMessages(channel, count, messagesOnlyFilter, null, null, null).get();
            return list.getMessages();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public void onBatchEnd(ServerConnectionData connection, BatchInfo batch) {
        passthru = true;
        for (Map.Entry<String, List<MessageInfo>> entry : channelData.entrySet()) {
            List<MessageInfo> currentMessages = getChannelMessageLog(connection, entry.getKey(), entry.getValue().size());
            int i;
            for (i = Math.min(entry.getValue().size(), currentMessages != null ? currentMessages.size() : 0); i >= 1; i--) {
                boolean matched = true;
                for (int j = 0; j < i; j++) {
                    MessageInfo l = entry.getValue().get(j);
                    MessageInfo r = currentMessages.get(currentMessages.size() - i + j);
                    if (!l.getMessage().equals(r.getMessage()) || !l.getSender().getNick().equals(r.getSender().getNick())) {
                        matched = false;
                        break;
                    }
                }
                if (matched)
                    break;
            }
            for ( ; i < entry.getValue().size(); i++) {
                MessageInfo message = entry.getValue().get(i);
                if (!connection.getMessageFilterList().filterMessage(connection, entry.getKey(), message))
                    return;
                try {
                    connection.getMessageStorageApi().addMessage(entry.getKey(), message, null, null).get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        passthru = false;
        channelData.clear();
    }

    static {
        messagesOnlyFilter = new MessageFilterOptions();
        messagesOnlyFilter.restrictToMessageTypes = new ArrayList<>();
        messagesOnlyFilter.restrictToMessageTypes.add(MessageInfo.MessageType.NORMAL);
        messagesOnlyFilter.restrictToMessageTypes.add(MessageInfo.MessageType.ME);
    }

}
