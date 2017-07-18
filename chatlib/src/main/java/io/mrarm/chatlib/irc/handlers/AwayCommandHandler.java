package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.NumericCommandHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.List;
import java.util.Map;

public class AwayCommandHandler extends NumericCommandHandler {

    public static final int RPL_AWAY = 301;

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_AWAY };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        String nick = params.get(1);
        String message = params.get(params.size() - 1);
        WhoisCommandHandler whoisHandler = connection.getCommandHandlerList().getHandler(WhoisCommandHandler.class);
        if (whoisHandler != null)
            whoisHandler.onAwayMessage(nick, message);
    }

}
