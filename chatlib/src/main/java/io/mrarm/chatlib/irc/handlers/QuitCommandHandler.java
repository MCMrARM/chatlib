package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class QuitCommandHandler implements CommandHandler {

    @Override
    public Object[] getHandledCommands() {
        return new Object[]{"QUIT"};
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

        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userInfo.getUUID());
            String message = CommandHandler.getParamOrNull(params, 0);
            for (String channel : userInfo.getChannels()) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                ChannelData.Member member = channelData.getMember(userInfo.getUUID());
                if (member != null)
                    channelData.removeMember(member);
                channelData.addMessage(new MessageInfo.Builder(senderInfo, message, MessageInfo.MessageType.QUIT),
                        tags);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a QUIT message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
