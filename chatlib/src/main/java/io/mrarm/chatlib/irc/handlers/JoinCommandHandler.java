package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class JoinCommandHandler implements CommandHandler {

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "JOIN" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        String[] channels = CommandHandler.getParamWithCheck(params, 0).split(",");
        if (sender.getNick().equals(connection.getUserNick())) {
            for (String channel : channels)
                connection.onChannelJoined(channel);
        }
        try {
            UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userUUID);
            for (String channel : channels) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                channelData.addMember(new ChannelData.Member(userUUID, null, null));
                channelData.addMessage(new MessageInfo.Builder(senderInfo, null, MessageInfo.MessageType.JOIN), tags);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
