package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.NickChangeMessageInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NickCommandHandler extends RequestResponseCommandHandler<String, NickCommandHandler.NickChangeCallback>
        implements CommandHandler {

    public static final int ERR_NONICKNAMEGIVEN = 431;
    public static final int ERR_NICKNAMEINUSE = 433;

    public NickCommandHandler(ErrorCommandHandler handler) {
        super(handler);
    }

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "NICK" };
    }

    @Override
    public int[] getHandledErrors() {
        return new int[] { ERR_NONICKNAMEGIVEN, ERR_NICKNAMEINUSE };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        String newNick = params.get(0);
        if (sender.getNick().equals(connection.getUserNick())) {
            connection.setUserNick(newNick);

            NickChangeCallback cb = requestResponseCallbacksFor(newNick);
            if (cb != null)
                cb.onNickChanged(newNick);
        }
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

    @Override
    public boolean onError(int commandId, List<String> params) {
        if (commandId == NickCommandHandler.ERR_NONICKNAMEGIVEN)
            return onError(null, commandId, params.get(1), false);
        return params.size() > 1 && onError(params.get(1), commandId, params.size() > 2 ? params.get(2) : null, false);
    }

    public interface NickChangeCallback {
        void onNickChanged(String newNick);
    }

}
