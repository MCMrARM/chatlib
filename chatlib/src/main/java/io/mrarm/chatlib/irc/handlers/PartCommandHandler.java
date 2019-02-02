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
    public Object[] getHandledCommands() {
        return new Object[]{"PART"};
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        if (sender == null) {
            // Apparently some broken IRCd or bouncers can send us messages without a prefix. We assume those are meant
            // to be processed as our client.
            sender = new MessagePrefix(connection.getUserNick());
        }

        String[] channels = CommandHandler.getParamWithCheck(params, 0).split(",");
        if (sender.getNick().equalsIgnoreCase(connection.getUserNick())) {
            for (String channel : channels)
                connection.onChannelLeft(channel);
        }
        try {
            UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userUUID);
            String message = CommandHandler.getParamOrNull(params, 1);
            for (String channel : channels) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                ChannelData.Member member = channelData.getMember(userUUID);
                if (member != null)
                    channelData.removeMember(member);
                channelData.addMessage(new MessageInfo.Builder(senderInfo, message, MessageInfo.MessageType.PART), tags);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a PART message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
