package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.WhoisInfo;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.NumericCommandHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhoisCommandHandler extends NumericCommandHandler {

    public static final int RPL_WHOISUSER = 311;
    public static final int RPL_WHOISSERVER = 312;
    public static final int RPL_WHOISOPERATOR = 313;
    public static final int RPL_WHOISIDLE = 317;
    public static final int RPL_ENDOFWHOIS = 318;
    public static final int RPL_WHOISCHANNELS = 319;
    public static final int RPL_WHOISACCOUNT = 330;
    public static final int RPL_WHOISSECURE = 671;

    private final Map<String, WhoisInfo.Builder> currentReply = new HashMap<>();
    private final Map<String, List<WhoisCallback>> callbacks = new HashMap<>();

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_WHOISUSER, RPL_WHOISSERVER, RPL_WHOISOPERATOR, RPL_WHOISIDLE, RPL_ENDOFWHOIS,
                RPL_WHOISCHANNELS, RPL_WHOISACCOUNT, RPL_WHOISSECURE };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        String nick = params.get(1);
        WhoisInfo.Builder builder = currentReply.get(nick);
        if (builder == null) {
            if (command == RPL_WHOISUSER) {
                builder = new WhoisInfo.Builder();
                currentReply.put(nick, builder);
            } else {
                throw new InvalidMessageException("Whois data not started with a RPL_WHOISUSER");
            }
        }
        if (command == RPL_WHOISUSER) {
            builder.setUserInfo(nick, params.get(2), params.get(3), params.get(5));
        } else if (command == RPL_WHOISSERVER) {
            builder.setServerInfo(params.get(2), params.get(3));
        } else if (command == RPL_WHOISOPERATOR) {
            builder.setOperator(true);
        } else if (command == RPL_WHOISIDLE) {
            builder.setIdle(Integer.parseInt(params.get(2)));
        } else if (command == RPL_WHOISCHANNELS) {
            for (String channel : params.get(2).split(" ")) {
                // it should be acceptable to use a NickPrefixParser here to get the channel modes and then wrap it into
                // a ChannelWithNickPrefixes
                NickWithPrefix p = connection.getNickPrefixParser().parse(connection, channel);
                builder.addChannel(new WhoisInfo.ChannelWithNickPrefixes(p.getNick(), p.getNickPrefixes()));
            }
        } else if (command == RPL_WHOISACCOUNT) {
            builder.setAccount(params.get(2));
        } else if (command == RPL_WHOISSECURE) {
            builder.setSecure(true);
        } else if (command == RPL_ENDOFWHOIS) {
            WhoisInfo info = builder.build();
            currentReply.remove(nick);

            synchronized (callbacks) {
                if (callbacks.containsKey(nick)) {
                    for (WhoisCallback callback : callbacks.get(nick))
                        callback.onWhoisInfoReceived(info);
                    callbacks.remove(nick);
                }
            }
        }
    }

    public void onAwayMessage(String nick, String message) {
        WhoisInfo.Builder builder = currentReply.get(nick);
        if (builder != null)
            builder.setAway(message);
    }

    public void onInfoRequested(String nick, WhoisCallback callback) {
        synchronized (callbacks) {
            if (!callbacks.containsKey(nick))
                callbacks.put(nick, new ArrayList<>());
            callbacks.get(nick).add(callback);
        }
    }

    public interface WhoisCallback {

        void onWhoisInfoReceived(WhoisInfo info);

    }

}
