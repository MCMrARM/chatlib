package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

public class JoinCommandHandler implements CommandHandler {

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       String params)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick()))
            connection.onChannelJoined(params);
        try {
            connection.getJoinedChannelData(params).addMessage(new MessageInfo(sender.getNick(),
                    null, MessageInfo.MessageType.JOIN));
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message");
        }
    }

}
