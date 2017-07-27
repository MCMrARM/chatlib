package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.NickChangeMessageInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NickCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "NICK" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        String newNick = params.get(0);
        if (sender.getNick().equals(connection.getUserNick()))
            connection.setUserNick(newNick);
        try {
            UserInfo userInfo = connection.getUserInfoApi().getUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(), sender.getHost(),
                    null, userInfo.getUUID());
            connection.getUserInfoApi().setUserNick(userInfo.getUUID(), params.get(0), null, null)
                    .get();
            for (String channel : userInfo.getChannels()) {
                try {
                    ChannelData channelData = connection.getJoinedChannelData(channel);
                    channelData.addMessage(new NickChangeMessageInfo.Builder(senderInfo, newNick), tags);
                    channelData.callMemberListChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
