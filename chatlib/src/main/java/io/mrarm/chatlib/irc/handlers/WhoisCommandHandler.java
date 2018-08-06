package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.WhoisInfo;
import io.mrarm.chatlib.dto.WhoisStatusMessageInfo;
import io.mrarm.chatlib.irc.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhoisCommandHandler extends RequestResponseCommandHandler<String, WhoisInfo> {

    public static final int RPL_WHOISUSER = 311;
    public static final int RPL_WHOISSERVER = 312;
    public static final int RPL_WHOISOPERATOR = 313;
    public static final int RPL_WHOISIDLE = 317;
    public static final int RPL_ENDOFWHOIS = 318;
    public static final int RPL_WHOISCHANNELS = 319;
    public static final int RPL_WHOISACCOUNT = 330;
    public static final int RPL_WHOISSECURE = 671;

    public static final int ERR_NOSUCHNICK = 401;

    private final Map<String, WhoisInfo.Builder> currentReply = new HashMap<>();

    public WhoisCommandHandler(ErrorCommandHandler handler) {
        super(handler, true);
    }

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_WHOISUSER, RPL_WHOISSERVER, RPL_WHOISOPERATOR, RPL_WHOISIDLE, RPL_ENDOFWHOIS,
                RPL_WHOISCHANNELS, RPL_WHOISACCOUNT, RPL_WHOISSECURE };
    }

    @Override
    public int[] getHandledErrors() {
        return new int[] { ERR_NOSUCHNICK, NickCommandHandler.ERR_NONICKNAMEGIVEN };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        String nick = CommandHandler.getParamWithCheck(params, 1).toLowerCase();
        WhoisInfo.Builder builder = currentReply.get(nick);
        if (builder == null) {
            if (numeric == RPL_WHOISUSER) {
                builder = new WhoisInfo.Builder();
                currentReply.put(nick, builder);
            } else {
                throw new InvalidMessageException("Whois data not started with a RPL_WHOISUSER");
            }
        }
        if (numeric == RPL_WHOISUSER) {
            builder.setUserInfo(nick, CommandHandler.getParamWithCheck(params, 2),
                    CommandHandler.getParamWithCheck(params, 3), CommandHandler.getParamOrNull(params, 5));
        } else if (numeric == RPL_WHOISSERVER) {
            builder.setServerInfo(CommandHandler.getParamWithCheck(params, 2),
                    CommandHandler.getParamWithCheck(params, 3));
        } else if (numeric == RPL_WHOISOPERATOR) {
            builder.setOperator(true);
        } else if (numeric == RPL_WHOISIDLE) {
            try {
                builder.setIdle(Integer.parseInt(CommandHandler.getParamWithCheck(params, 2)));
            } catch (NumberFormatException e) {
                throw new InvalidMessageException("Bad idle time");
            }
        } else if (numeric == RPL_WHOISCHANNELS) {
            for (String channel : CommandHandler.getParamWithCheck(params, 2).split(" ")) {
                // it should be acceptable to use a NickPrefixParser here to get the channel modes and then wrap it into
                // a ChannelWithNickPrefixes
                NickWithPrefix p = connection.getNickPrefixParser().parse(connection, channel);
                builder.addChannel(new WhoisInfo.ChannelWithNickPrefixes(p.getNick(), p.getNickPrefixes()));
            }
        } else if (numeric == RPL_WHOISACCOUNT) {
            builder.setAccount(CommandHandler.getParamWithCheck(params, 2));
        } else if (numeric == RPL_WHOISSECURE) {
            builder.setSecure(true);
        } else if (numeric == RPL_ENDOFWHOIS) {
            currentReply.remove(nick);

            WhoisInfo whoisInfo = builder.build();
            onResponse(nick, whoisInfo);
            connection.getServerStatusData().addMessage(new WhoisStatusMessageInfo(
                    sender != null ? sender.getServerName() : null, new Date(), whoisInfo));
        }
    }

    @Override
    public boolean onError(int commandId, List<String> params) {
        if (commandId == NickCommandHandler.ERR_NONICKNAMEGIVEN)
            return onError(null, commandId, CommandHandler.getParamOrNull(params, 1), false);
        return params.size() > 1 && onError(params.get(1).toLowerCase(), commandId,
                CommandHandler.getParamOrNull(params, 2), false);
    }

    @Override
    public void onDisconnected() {
        super.onDisconnected();
        currentReply.clear();
    }

    @Override
    public boolean onRequested(String i, Callback<WhoisInfo> callback, ErrorCallback<String> errorCallback) {
        return super.onRequested(i.toLowerCase(), callback, errorCallback);
    }

    public void onAwayMessage(String nick, String message) {
        WhoisInfo.Builder builder = currentReply.get(nick);
        if (builder != null)
            builder.setAway(message);
    }

}
