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
    public String[] getHandledCommands() {
        return new String[]{"QUIT"};
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userInfo.getUUID());
            for (String channel : userInfo.getChannels()) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                channelData.removeMember(channelData.getMember(userInfo.getUUID()));
                channelData.addMessage(new MessageInfo.Builder(senderInfo, params.get(0), MessageInfo.MessageType.QUIT), tags);
            }
        } catch (InterruptedException | ExecutionException | NoSuchChannelException e) {
            throw new RuntimeException(e);
        }
    }

}
