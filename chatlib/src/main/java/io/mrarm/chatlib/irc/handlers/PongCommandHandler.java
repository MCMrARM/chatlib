package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;

public class PongCommandHandler extends RequestResponseCommandHandler<String, Void> {

    public PongCommandHandler(ErrorCommandHandler handler) {
        super(handler, false);
    }

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "PONG" };
    }

    @Override
    public int[] getHandledErrors() {
        return new int[0];
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        onResponse(CommandHandler.getParamOrNull(params, 1), null);
    }

    @Override
    public boolean onError(int commandId, List<String> params) {
        return true;
    }

}
