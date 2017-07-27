package io.mrarm.chatlib.irc.filters;

import io.mrarm.chatlib.dto.BatchInfo;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.MessageListAfterIdentifier;
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

    // Request this many additional messages when getting the channel log in order to prevent having to do more calls
    // (we aren't interested in all channel messages, we'll discard all event messages so to account for the messages
    // we will discard we will use this variable)
    private static final int REQUEST_EXTRA_MESSAGE_COUNT = 50;

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
        List<MessageInfo> ret = new ArrayList<>();
        try {
            MessageListAfterIdentifier afterId = null;
            List<MessageInfo> filtered = new ArrayList<>();
            while (true) {
                filtered.clear();
                MessageList list = connection.getMessageStorageApi().getMessages(channel, count - ret.size() + REQUEST_EXTRA_MESSAGE_COUNT, afterId, null, null).get();
                for (MessageInfo msg : list.getMessages()) {
                    if (msg.getType() == MessageInfo.MessageType.NORMAL || msg.getType() == MessageInfo.MessageType.ME)
                        filtered.add(msg);
                }
                ret.addAll(0, filtered);
                if (ret.size() >= count)
                    return ret;
                afterId = list.getAfterIdentifier();
                if (afterId == null)
                    break;
            }
        } catch (Exception ignored) {
        }
        return ret;
    }

    @Override
    public void onBatchEnd(ServerConnectionData connection, BatchInfo batch) {
        passthru = true;
        for (Map.Entry<String, List<MessageInfo>> entry : channelData.entrySet()) {
            List<MessageInfo> currentMessages = getChannelMessageLog(connection, entry.getKey(), entry.getValue().size());
            int i;
            for (i = Math.min(entry.getValue().size(), currentMessages.size()); i >= 1; i--) {
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

}
