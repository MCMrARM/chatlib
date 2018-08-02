package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MotdCommandHandler implements CommandDisconnectHandler {

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
                motdBuilder = new StringBuilder(CommandHandler.getParamWithCheck(params, 1));
                motdBuilder.append('\n');
                break;
            case RPL_MOTD:
                if (motdBuilder == null)
                    throw new InvalidMessageException();
                motdBuilder.append(CommandHandler.getParamWithCheck(params, 1));
                motdBuilder.append('\n');
                break;
            case ERR_NOMOTD:
            case RPL_ENDOFMOTD: {
                String motd = (motdBuilder == null ? CommandHandler.getParamWithCheck(params, 1) :
                        motdBuilder.toString());
                connection.getServerStatusData().setMotd(motd);
                connection.getServerStatusData().addMessage(new StatusMessageInfo(
                        sender != null ? sender.getServerName() : null, new Date(),
                        StatusMessageInfo.MessageType.MOTD, motd));
                motdBuilder = null;
                connection.getApi().notifyMotdReceived();
                break;
            }
        }
    }

    @Override
    public void onDisconnected() {
        motdBuilder = null;
    }

}
