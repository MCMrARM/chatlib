package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.concurrent.ExecutionException;

public class JoinCommandHandler implements CommandHandler {

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       String params)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick()))
            connection.onChannelJoined(params);
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    userInfo.getUUID());
            connection.getJoinedChannelData(params).addMessage(new MessageInfo(senderInfo, null,
                    MessageInfo.MessageType.JOIN));
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
