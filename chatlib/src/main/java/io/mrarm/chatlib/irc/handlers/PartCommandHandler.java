package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PartCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[]{"PART"};
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick())) {
            for (String channel : params.get(0).split(","))
                connection.onChannelLeft(channel);
        }
        try {
            UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userUUID);
            String channel = params.get(0);
            ChannelData channelData = connection.getJoinedChannelData(channel);
            channelData.removeMember(channelData.getMember(userUUID));
            channelData.addMessage(new MessageInfo.Builder(senderInfo, params.get(1), MessageInfo.MessageType.PART), tags);
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
