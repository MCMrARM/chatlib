package io.mrarm.chatlib.irc;

public interface CommandHandler {

    void handle(ServerConnectionData connection, MessagePrefix sender, String command, String params)
            throws InvalidMessageException;

}
