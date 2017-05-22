package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.user.UserInfo;

import java.util.concurrent.ExecutionException;

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
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    userInfo.getUUID());
            connection.getJoinedChannelData(target).addMessage(new MessageInfo(senderInfo, message,
                    MessageInfo.MessageType.NORMAL));
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a PRIVMSG message");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
