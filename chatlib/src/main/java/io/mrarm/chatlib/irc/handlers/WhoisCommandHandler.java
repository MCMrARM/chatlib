package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.WhoisInfo;
import io.mrarm.chatlib.irc.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhoisCommandHandler extends RequestResponseCommandHandler<String, WhoisCommandHandler.WhoisCallback> {

    public static final int RPL_WHOISUSER = 311;
    public static final int RPL_WHOISSERVER = 312;
    public static final int RPL_WHOISOPERATOR = 313;
    public static final int RPL_WHOISIDLE = 317;
    public static final int RPL_ENDOFWHOIS = 318;
    public static final int RPL_WHOISCHANNELS = 319;
    public static final int RPL_WHOISACCOUNT = 330;
    public static final int RPL_WHOISSECURE = 671;

    private final Map<String, WhoisInfo.Builder> currentReply = new HashMap<>();

    public WhoisCommandHandler(ErrorCommandHandler handler) {
        super(handler);
    }

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_WHOISUSER, RPL_WHOISSERVER, RPL_WHOISOPERATOR, RPL_WHOISIDLE, RPL_ENDOFWHOIS,
                RPL_WHOISCHANNELS, RPL_WHOISACCOUNT, RPL_WHOISSECURE };
    }

    @Override
    public int[] getHandledErrors() {
        return new int[] { NickCommandHandler.ERR_NICKNAMEINUSE, NickCommandHandler.ERR_NONICKNAMEGIVEN };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        String nick = params.get(1);
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
            builder.setUserInfo(nick, params.get(2), params.get(3), params.get(5));
        } else if (numeric == RPL_WHOISSERVER) {
            builder.setServerInfo(params.get(2), params.get(3));
        } else if (numeric == RPL_WHOISOPERATOR) {
            builder.setOperator(true);
        } else if (numeric == RPL_WHOISIDLE) {
            builder.setIdle(Integer.parseInt(params.get(2)));
        } else if (numeric == RPL_WHOISCHANNELS) {
            for (String channel : params.get(2).split(" ")) {
                // it should be acceptable to use a NickPrefixParser here to get the channel modes and then wrap it into
                // a ChannelWithNickPrefixes
                NickWithPrefix p = connection.getNickPrefixParser().parse(connection, channel);
                builder.addChannel(new WhoisInfo.ChannelWithNickPrefixes(p.getNick(), p.getNickPrefixes()));
            }
        } else if (numeric == RPL_WHOISACCOUNT) {
            builder.setAccount(params.get(2));
        } else if (numeric == RPL_WHOISSECURE) {
            builder.setSecure(true);
        } else if (numeric == RPL_ENDOFWHOIS) {
            WhoisInfo info = builder.build();
            currentReply.remove(nick);

            WhoisCallback cb = requestResponseCallbacksFor(nick);
            if (cb != null)
                cb.onWhoisInfoReceived(info);
        }
    }

    @Override
    public boolean onError(int commandId, List<String> params) {
        return params.size() > 1 && onError(params.get(1), commandId, params.size() >= 2 ? params.get(2) : null, false);
    }

    public void onAwayMessage(String nick, String message) {
        WhoisInfo.Builder builder = currentReply.get(nick);
        if (builder != null)
            builder.setAway(message);
    }

    public interface WhoisCallback {

        void onWhoisInfoReceived(WhoisInfo info);

    }

}
