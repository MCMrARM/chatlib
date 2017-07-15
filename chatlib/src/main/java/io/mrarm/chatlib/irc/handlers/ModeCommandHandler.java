package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.ModeList;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ModeCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[]{"MODE", "324"};
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        if (sender.getNick().equals(connection.getUserNick())) {
            for (String channel : params.get(0).split(","))
                connection.onChannelLeft(channel);
        }
        try {
            String target = params.get(0);
            boolean isChannelTarget = connection.getSupportList().getSupportedChannelTypes().contains(target.charAt(0));
            if (isChannelTarget) {
                ChannelData channelData = connection.getJoinedChannelData(target);
                handleChannelModes(connection, channelData, params);
            } else {
                // TODO: user modes
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a JOIN message", e);
        }
    }

    private void handleChannelModes(ServerConnectionData connection, ChannelData channelData, List<String> params) throws InvalidMessageException {
        int argIndex = 1;
        String modes = params.get(argIndex++);
        boolean add = true;
        for (int i = 0; i < modes.length(); i++) {
            char c = modes.charAt(i);
            if (c == '+') {
                add = true;
                continue;
            } else if (c == '-') {
                add = false;
                continue;
            } else if (connection.getSupportList().getSupportedNickPrefixModes().contains(c)) {
                ChannelData.Member member = getUser(connection, channelData, params.get(argIndex++));
                addPrefix(connection, channelData, member, connection.getSupportList().getSupportedNickPrefixes().get(
                        connection.getSupportList().getSupportedNickPrefixModes().find(c)));
            } else if (connection.getSupportList().getSupportedFlagChannelModes().contains(c)) {
                channelData.setFlagMode(c, add);
            } else if (connection.getSupportList().getSupportedListChannelModes().contains(c)) {
                if (add)
                    channelData.addListMode(c, params.get(argIndex++));
                else
                    channelData.removeListMode(c, params.get(argIndex++));
            } else if (connection.getSupportList().getSupportedValueExactUnsetChannelModes().contains(c)) {
                String p = params.get(argIndex++);
                if (add)
                    channelData.setValueExactUnsetMode(c, p);
                else
                    channelData.removeValueExactUnsetMode(c);
            } else if (connection.getSupportList().getSupportedValueChannelModes().contains(c)) {
                if (add)
                    channelData.setValueMode(c, params.get(argIndex++));
                else
                    channelData.removeValueMode(c);
            } else {
                throw new InvalidMessageException("Unknown channel mode: " + c);
            }
        }
    }

    private ChannelData.Member getUser(ServerConnectionData connection, ChannelData channelData, String nick) {
        try {
            UUID userUUID = connection.getUserInfoApi().resolveUser(nick, null, null, null, null).get();
            return channelData.getMember(userUUID);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void addMode(ChannelData channel, ChannelData.Member member, char mode) {
        if (member.getModeList().contains(mode))
            return;
        channel.setMemberModeList(member, new ModeList(member.getModeList().toString() + mode));
    }

    private void addPrefix(ServerConnectionData connection, ChannelData channel, ChannelData.Member member, char mode) {
        if (member.getNickPrefixes() == null) {
            channel.setMemberNickPrefixes(member, new NickPrefixList(String.valueOf(mode)));
            return;
        }
        if (member.getNickPrefixes().contains(mode))
            return;
        String prefixes = member.getNickPrefixes().toString();
        ModeList modes = connection.getSupportList().getSupportedNickPrefixes();
        int myModeRank = modes.find(mode);
        for (int insertAfter = 0; insertAfter < prefixes.length(); insertAfter++) {
            int rank = modes.find(member.getNickPrefixes().get(prefixes.charAt(insertAfter)));
            if (myModeRank < rank) {
                channel.setMemberNickPrefixes(member, new NickPrefixList(prefixes.substring(0, insertAfter) + mode + prefixes.substring(insertAfter)));
                return;
            }
        }
        channel.setMemberNickPrefixes(member, new NickPrefixList(prefixes + mode));
    }

}
