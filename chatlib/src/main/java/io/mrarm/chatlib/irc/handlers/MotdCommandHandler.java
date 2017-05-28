package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.NumericCommandHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.util.Date;
import java.util.List;

public class MotdCommandHandler extends NumericCommandHandler {

    public static final int RPL_MOTDSTART = 375;
    public static final int RPL_MOTD = 372;
    public static final int RPL_ENDOFMOTD = 376;

    private StringBuilder motdBuilder = null;

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_MOTDSTART, RPL_MOTD, RPL_ENDOFMOTD };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command, List<String> params)
            throws InvalidMessageException {
        switch (command) {
            case RPL_MOTDSTART:
                motdBuilder = new StringBuilder(params.get(1));
                motdBuilder.append('\n');
                break;
            case RPL_MOTD:
                motdBuilder.append(params.get(1));
                motdBuilder.append('\n');
                break;
            case RPL_ENDOFMOTD: {
                motdBuilder.append(params.get(1));
                String motd = motdBuilder.toString();
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
