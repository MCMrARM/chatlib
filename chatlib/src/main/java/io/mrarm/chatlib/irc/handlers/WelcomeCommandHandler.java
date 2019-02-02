package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.HostInfoMessageInfo;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class WelcomeCommandHandler implements CommandHandler {

    public static final int RPL_WELCOME = 1;
    public static final int RPL_YOURHOST = 2;
    public static final int RPL_CREATED = 3;
    public static final int RPL_MYINFO = 4;
    public static final int RPL_BOUNCE = 10;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_WELCOME, RPL_YOURHOST, RPL_CREATED, RPL_MYINFO, RPL_BOUNCE };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        StatusMessageInfo.MessageType type = StatusMessageInfo.MessageType.WELCOME_TEXT;
        switch (numeric) {
            case RPL_YOURHOST:
                type = StatusMessageInfo.MessageType.YOUR_HOST_TEXT;
                break;
            case RPL_CREATED:
                type = StatusMessageInfo.MessageType.SERVER_CREATED_TEXT;
                break;
            case RPL_MYINFO: {
                type = StatusMessageInfo.MessageType.HOST_INFO;
                connection.getServerStatusData().addMessage(new HostInfoMessageInfo(sender != null ?
                        sender.getServerName() : null, new Date(), type,
                        CommandHandler.getParamWithCheck(params, 1),
                        CommandHandler.getParamWithCheck(params, 2),
                        CommandHandler.getParamOrNull(params, 3),
                        CommandHandler.getParamOrNull(params, 4)));
                return;
            }
            case RPL_BOUNCE:
                type = StatusMessageInfo.MessageType.REDIR_TEXT;
                break;
        }
        String text = CommandHandler.getParamWithCheck(params, 1);
        if (params.size() > 1) {
            StringBuilder s = new StringBuilder();
            s.append(params.get(1));
            for (int i = 2; i < params.size(); i++) {
                s.append(' ');
                s.append(params.get(i));
            }
            text = s.toString();
        }
        connection.getServerStatusData().addMessage(new StatusMessageInfo(sender != null ?
                sender.getServerName() : null, new Date(), type, text));
    }
}
