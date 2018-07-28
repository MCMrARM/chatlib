package io.mrarm.chatlib.irc.handlers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.KickMessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.ChannelData;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

public class KickCommandHandler implements CommandHandler {

    @Override
    public Object[] getHandledCommands() {
        return new Object[]{"KICK"};
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

        String channel = CommandHandler.getParamWithCheck(params, 0);
        String kicked = CommandHandler.getParamWithCheck(params, 1);
        if (kicked.equalsIgnoreCase(connection.getUserNick())) {
            connection.onChannelLeft(channel);
        }
        try {
            UUID senderUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            UUID kickedUUID = connection.getUserInfoApi().resolveUser(kicked, null, null, null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, senderUUID);
            String message = CommandHandler.getParamOrNull(params, 2);

            ChannelData channelData = connection.getJoinedChannelData(channel);
            ChannelData.Member member = channelData.getMember(kickedUUID);
            if (member != null)
                channelData.removeMember(member);
            channelData.addMessage(new KickMessageInfo.Builder(senderInfo, kicked, message), tags);

        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a KICK message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
