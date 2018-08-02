package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.TopicWhoTimeMessageInfo;
import io.mrarm.chatlib.irc.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TopicWhoTimeCommandHandler implements CommandHandler {

    public static final int RPL_TOPICWHOTIME = 333;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_TOPICWHOTIME };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        String who = CommandHandler.getParamWithCheck(params, 2);
        Date when = new Date(Long.parseLong(CommandHandler.getParamWithCheck(params, 3)) * 1000L);
        try {
            ChannelData channelData = connection.getJoinedChannelData(CommandHandler.getParamWithCheck(params, 1));

            if (!who.equals(channelData.getTopicSetBy()) || !when.equals(channelData.getTopicSetOn())) {
                MessagePrefix prefix = new MessagePrefix(who);
                UUID userUUID = null;
                try {
                    userUUID = connection.getUserInfoApi().resolveUser(
                            prefix.getNick(), prefix.getUser(), prefix.getHost(), null, null).get();
                } catch (Exception ignored) {
                }
                channelData.addMessage(new TopicWhoTimeMessageInfo.Builder(null,
                        prefix.toSenderInfo(userUUID, null), when), tags);
                channelData.setTopic(channelData.getTopic(), prefix.toSenderInfo(userUUID, null), when);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a topic message", e);
        }
    }

}
