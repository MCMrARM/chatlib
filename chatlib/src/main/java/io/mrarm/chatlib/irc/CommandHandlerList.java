package io.mrarm.chatlib.irc;

import java.util.HashMap;

import io.mrarm.chatlib.irc.handlers.*;

public class CommandHandlerList {

    private HashMap<String, CommandHandler> handlers = new HashMap<>();
    private static CommandHandlerList defaultHandlers;

    public void addDefaultHandlers() {
        if (defaultHandlers == null) {
            defaultHandlers = new CommandHandlerList();
            defaultHandlers.registerHandler(new JoinCommandHandler());
            defaultHandlers.registerHandler(new MessageCommandHandler());
            defaultHandlers.registerHandler(new NickCommandHandler());
            defaultHandlers.registerHandler(new WelcomeCommandHandler());
            defaultHandlers.registerHandler(new PingCommandHandler());
        }
        handlers.putAll(defaultHandlers.handlers);

        // per-connection handlers
        registerHandler(new NamesReplyCommandHandler());
        registerHandler(new MotdCommandHandler());
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
