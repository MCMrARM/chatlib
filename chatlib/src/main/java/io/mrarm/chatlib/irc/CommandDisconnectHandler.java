package io.mrarm.chatlib.irc;

/**
 * A CommandHandler that requests a disconnects notification.
 */
public interface CommandDisconnectHandler extends CommandHandler {

    void onDisconnected();

}
