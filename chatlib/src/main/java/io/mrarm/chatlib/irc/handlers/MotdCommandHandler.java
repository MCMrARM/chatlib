package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MotdCommandHandler implements CommandHandler {

    public static final int RPL_MOTDSTART = 375;
    public static final int RPL_MOTD = 372;
    public static final int RPL_ENDOFMOTD = 376;
    public static final int ERR_NOMOTD = 422;

    private StringBuilder motdBuilder = null;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_MOTDSTART, RPL_MOTD, RPL_ENDOFMOTD, ERR_NOMOTD };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        switch (numeric) {
            case RPL_MOTDSTART:
                motdBuilder = new StringBuilder(params.get(1));
                motdBuilder.append('\n');
                break;
            case RPL_MOTD:
                motdBuilder.append(params.get(1));
                motdBuilder.append('\n');
                break;
            case ERR_NOMOTD:
            case RPL_ENDOFMOTD: {
                if (numeric != ERR_NOMOTD)
                    motdBuilder.append(params.get(1));
                String motd = (numeric == ERR_NOMOTD ? params.get(1) : motdBuilder.toString());
                connection.getServerStatusData().setMotd(motd);
                connection.getServerStatusData().addMessage(new StatusMessageInfo(sender.getServerName(), new Date(),
                        StatusMessageInfo.MessageType.MOTD, motd));
                motdBuilder = null;
                connection.getApi().notifyMotdReceived();
                break;
            }
        }
    }
}
