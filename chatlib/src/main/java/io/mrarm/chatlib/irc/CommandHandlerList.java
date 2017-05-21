package io.mrarm.chatlib.irc;

import java.util.HashMap;

import io.mrarm.chatlib.irc.handlers.JoinCommandHandler;
import io.mrarm.chatlib.irc.handlers.PrivMsgCommandHandler;

public class CommandHandlerList {

    private HashMap<String, CommandHandler> handlers = new HashMap<>();
    private static HashMap<String, CommandHandler> defaultHandlers;

    public void addDefaultHandlers() {
        if (defaultHandlers == null) {
            defaultHandlers = new HashMap<>();
            defaultHandlers.put("JOIN", new JoinCommandHandler());
            defaultHandlers.put("PRIVMSG", new PrivMsgCommandHandler());
        }
        handlers.putAll(defaultHandlers);
    }

    public CommandHandler getHandlerFor(String command) throws InvalidMessageException {
        if (!handlers.containsKey(command))
            throw new InvalidMessageException("No such command found (" + command + ")");
        return handlers.get(command);
    }

    public void registerHandler(String command, CommandHandler handler) {
        handlers.put(command, handler);
    }

    public void registerHandler(int command, CommandHandler handler) {
        handlers.put(Integer.toString(command), handler);
    }

}
