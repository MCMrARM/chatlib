package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.*;
import io.mrarm.chatlib.irc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ModeCommandHandler implements CommandHandler {

    public static final int RPL_CHANNELMODEIS = 324;

    @Override
    public Object[] getHandledCommands() {
        return new Object[]{"MODE", RPL_CHANNELMODEIS};
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        try {
            UUID userUUID = sender != null ? connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get() : null;

            String target = CommandHandler.getParamWithCheck(params, 0);
            boolean isChannelTarget = connection.getSupportList().getSupportedChannelTypes().contains(target.charAt(0));
            if (isChannelTarget) {
                ChannelData channelData = connection.getJoinedChannelData(target);
                MessageSenderInfo senderInfo = sender != null ? sender.toSenderInfo(userUUID, channelData) : null;
                handleChannelModes(connection, senderInfo, channelData, params, tags);
            } else {
                // TODO: user modes
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a MODE message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleChannelModes(ServerConnectionData connection, MessageSenderInfo senderInfo,
                                    ChannelData channelData, List<String> params, Map<String, String> tags) throws InvalidMessageException {
        int argIndex = 1;
        String modes = CommandHandler.getParamWithCheck(params, argIndex++);
        List<ChannelModeMessageInfo.Entry> log = new ArrayList<>();
        boolean add = true;
        for (int i = 0; i < modes.length(); i++) {
            char c = modes.charAt(i);
            if (c == '+') {
                add = true;
            } else if (c == '-') {
                add = false;
            } else if (connection.getSupportList().getSupportedNickPrefixModes().contains(c)) {
                String nick = CommandHandler.getParamWithCheck(params, argIndex++);
                ChannelData.Member member = getUser(connection, channelData, nick);
                char prefix = connection.getSupportList().getSupportedNickPrefixes().get(
                        connection.getSupportList().getSupportedNickPrefixModes().find(c));
                if (member != null) {
                    if (add) {
                        addMode(channelData, member, c);
                        addPrefix(connection, channelData, member, prefix);
                    } else {
                        removeMode(channelData, member, c);
                        removePrefix(channelData, member, prefix);
                    }
                }
                log.add(new ChannelModeMessageInfo.Entry(ChannelModeMessageInfo.EntryType.NICK_FLAG, c, nick, !add));
            } else if (connection.getSupportList().getSupportedFlagChannelModes().contains(c)) {
                channelData.setFlagMode(c, add);
                log.add(new ChannelModeMessageInfo.Entry(ChannelModeMessageInfo.EntryType.FLAG, c, null, !add));
            } else if (connection.getSupportList().getSupportedListChannelModes().contains(c)) {
                String p = CommandHandler.getParamWithCheck(params, argIndex++);
                if (add)
                    channelData.addListMode(c, p);
                else
                    channelData.removeListMode(c, p);
                log.add(new ChannelModeMessageInfo.Entry(ChannelModeMessageInfo.EntryType.LIST, c, p, !add));
            } else if (connection.getSupportList().getSupportedValueExactUnsetChannelModes().contains(c)) {
                String p = CommandHandler.getParamWithCheck(params, argIndex++);
                if (add)
                    channelData.setValueExactUnsetMode(c, p);
                else
                    channelData.removeValueExactUnsetMode(c);
                log.add(new ChannelModeMessageInfo.Entry(ChannelModeMessageInfo.EntryType.VALUE_EXACT_UNSET, c, p, !add));
            } else if (connection.getSupportList().getSupportedValueChannelModes().contains(c)) {
                String p = (add ? CommandHandler.getParamWithCheck(params, argIndex++) : null);
                if (add)
                    channelData.setValueMode(c, p);
                else
                    channelData.removeValueMode(c);
                log.add(new ChannelModeMessageInfo.Entry(ChannelModeMessageInfo.EntryType.VALUE, c, p, !add));
            } else {
                throw new InvalidMessageException("Unknown channel mode: " + c);
            }
        }
        channelData.addMessage(new ChannelModeMessageInfo.Builder(senderInfo, log), tags);
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
        if (member.getModeList() == null) {
            channel.setMemberModeList(member, new ModeList(String.valueOf(mode)));
            return;
        }
        if (member.getModeList().contains(mode))
            return;
        channel.setMemberModeList(member, new ModeList(member.getModeList().toString() + mode));
    }

    private void removeMode(ChannelData channel, ChannelData.Member member, char mode) {
        if (member.getModeList() == null)
            return;
        int i = member.getModeList().find(mode);
        if (i == -1)
            return;
        if (member.getModeList().length() == 1) {
            channel.setMemberModeList(member, null);
            return;
        }
        String list = member.getModeList().toString();
        channel.setMemberModeList(member, new ModeList(list.substring(0, i) + list.substring(i + 1)));
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
            int rank = modes.find(prefixes.charAt(insertAfter));
            if (myModeRank < rank) {
                channel.setMemberNickPrefixes(member, new NickPrefixList(prefixes.substring(0, insertAfter) + mode + prefixes.substring(insertAfter)));
                return;
            }
        }
        channel.setMemberNickPrefixes(member, new NickPrefixList(prefixes + mode));
    }

    private void removePrefix(ChannelData channel, ChannelData.Member member, char mode) {
        if (member.getNickPrefixes() == null)
            return;
        int i = member.getNickPrefixes().find(mode);
        if (i == -1)
            return;
        if (member.getNickPrefixes().length() == 1) {
            channel.setMemberNickPrefixes(member, null);
            return;
        }
        String prefixes = member.getNickPrefixes().toString();
        channel.setMemberNickPrefixes(member, new NickPrefixList(prefixes.substring(0, i) + prefixes.substring(i + 1)));
    }

}
