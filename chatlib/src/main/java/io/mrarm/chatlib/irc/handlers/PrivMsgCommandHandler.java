package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

public class PrivMsgCommandHandler implements CommandHandler {

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command,
                       String params)
            throws InvalidMessageException {
        int iof = params.indexOf(' ');
        if (iof == -1 || params.charAt(iof + 1) != ':')
            throw new InvalidMessageException("Invalid PRIVMSG message");
        String target = params.substring(0, iof);
        String message = params.substring(iof + 2);
        try {
            connection.getJoinedChannelData(target).addMessage(new MessageInfo(sender.getNick(),
                    message, MessageInfo.MessageType.NORMAL));
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a PRIVMSG message");
        }
    }

}
