package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JoinCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "JOIN" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       List<String> params)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick())) {
            for (String channel : params.get(0).split(","))
                connection.onChannelJoined(channel);
        }
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userInfo.getUUID());
            for (String channel : params.get(0).split(","))
                connection.getJoinedChannelData(channel).addMessage(new MessageInfo(senderInfo, new Date(), null,
                        MessageInfo.MessageType.JOIN));
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
