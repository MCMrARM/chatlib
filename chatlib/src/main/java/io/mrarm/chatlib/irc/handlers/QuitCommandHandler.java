package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class QuitCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[]{"QUIT"};
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
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userInfo.getUUID());
            for (String channel : userInfo.getChannels()) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                channelData.removeMember(channelData.getMember(userInfo.getUUID()));
                connection.getMessageStorageApi().addMessage(channel, new MessageInfo(senderInfo, new Date(),
                        params.get(0), MessageInfo.MessageType.QUIT), null, null).get();
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a QUIT message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
