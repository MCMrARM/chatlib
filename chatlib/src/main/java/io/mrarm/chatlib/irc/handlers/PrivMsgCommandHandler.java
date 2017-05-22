package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.user.UserInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class PrivMsgCommandHandler implements CommandHandler {

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params)
            throws InvalidMessageException {
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    userInfo.getUUID());
            MessageInfo message = new MessageInfo(senderInfo, params.get(1), MessageInfo.MessageType.NORMAL);
            for (String channel : params.get(0).split(","))
                connection.getJoinedChannelData(channel).addMessage(message);
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a PRIVMSG message");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
