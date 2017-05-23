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
            defaultHandlers.registerHandler("JOIN", new JoinCommandHandler());
            defaultHandlers.registerHandler("PRIVMSG", new PrivMsgCommandHandler());
            defaultHandlers.registerHandler("NICK", new NickCommandHandler());
        }
        handlers.putAll(defaultHandlers.handlers);

        // per-connection handlers
        NamesReplyCommandHandler namesHandler = new NamesReplyCommandHandler();
        registerHandler(NamesReplyCommandHandler.RPL_NAMREPLY, namesHandler);
        registerHandler(NamesReplyCommandHandler.RPL_ENDOFNAMES, namesHandler);
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
