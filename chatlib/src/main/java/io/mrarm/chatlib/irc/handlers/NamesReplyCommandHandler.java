package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NamesReplyCommandHandler extends NumericCommandHandler {

    public static final int RPL_NAMREPLY = 353;
    public static final int RPL_ENDOFNAMES = 366;

    private Map<String, List<ChannelData.Member>> channelNamesList = new HashMap<>();

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_NAMREPLY, RPL_ENDOFNAMES };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command,
                       List<String> params) throws InvalidMessageException {
        if (command == RPL_NAMREPLY) {
            int paramId = 1;
            String channelName = params.get(paramId);
            // if the first argument is '=' or '*' or '@', skip it as it's the channel type which we don't really care
            // about for now
            if (channelName.length() == 1 && (channelName.charAt(0) == '=' || channelName.charAt(0) == '*' ||
                    channelName.charAt(0) == '@'))
                channelName = params.get(++paramId);

            List<ChannelData.Member> list = channelNamesList.computeIfAbsent(channelName, k -> new ArrayList<>());
            for ( ; paramId < params.size(); paramId++) {
                NickWithPrefix nickWithPrefix = connection.getNickPrefixParser().parse(params.get(paramId));
                UserInfo userInfo;
                try {
                    userInfo = connection.getUserInfoApi().getUser(nickWithPrefix.getNick(), null, null,
                            null, null).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    continue;
                }
                list.add(new ChannelData.Member(userInfo, nickWithPrefix.getNickPrefixes()));
            }
        } else if (command == RPL_ENDOFNAMES) {
            String channelName = params.get(1);
            try {
                connection.getJoinedChannelData(channelName).setMembers(channelNamesList.get(channelName));
            } catch (NoSuchChannelException e) {
                throw new InvalidMessageException("Invalid channel name", e);
            }
        }
    }
}
