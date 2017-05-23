package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PrivMsgCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "PRIVMSG" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params)
            throws InvalidMessageException {
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            for (String channel : params.get(0).split(",")) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                ChannelData.Member memberInfo = channelData.getMember(userInfo);
                MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(),
                        sender.getHost(), memberInfo != null ? memberInfo.getNickPrefixes() : null, userInfo.getUUID());
                MessageInfo message = new MessageInfo(senderInfo, new Date(), params.get(1),
                        MessageInfo.MessageType.NORMAL);
                channelData.addMessage(message);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a PRIVMSG message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
