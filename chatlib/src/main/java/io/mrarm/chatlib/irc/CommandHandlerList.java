package io.mrarm.chatlib.irc;

import java.util.HashMap;

import io.mrarm.chatlib.irc.handlers.JoinCommandHandler;
import io.mrarm.chatlib.irc.handlers.NamesReplyCommandHandler;
import io.mrarm.chatlib.irc.handlers.NickCommandHandler;
import io.mrarm.chatlib.irc.handlers.PrivMsgCommandHandler;

public class CommandHandlerList {

    private HashMap<String, CommandHandler> handlers = new HashMap<>();
    private static CommandHandlerList defaultHandlers;

    public void addDefaultHandlers() {
        if (defaultHandlers == null) {
            defaultHandlers = new CommandHandlerList();
            defaultHandlers.registerHandler(new JoinCommandHandler());
            defaultHandlers.registerHandler(new PrivMsgCommandHandler());
            defaultHandlers.registerHandler(new NickCommandHandler());
        }
        handlers.putAll(defaultHandlers.handlers);

        // per-connection handlers
        registerHandler(new NamesReplyCommandHandler());
    }

    public CommandHandler getHandlerFor(String command) throws InvalidMessageException {
        if (!handlers.containsKey(command))
            throw new InvalidMessageException("No such command found (" + command + ")");
        return handlers.get(command);
    }

    public void registerHandler(CommandHandler handler) {
        for (String command : handler.getHandledCommands())
            handlers.put(command, handler);
    }

}
