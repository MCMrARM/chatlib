package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.irc.cap.Capability;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MessageCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "PRIVMSG", "NOTICE" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        try {
            MessageInfo.MessageType type = (command.equals("NOTICE") ? MessageInfo.MessageType.NOTICE :
                    MessageInfo.MessageType.NORMAL);
            UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            String[] targetChannels = params.get(0).split(",");

            String text = params.get(1);
            if (text.indexOf('\20') != -1)
                text = lowDequote(text);
            int ctcpS = text.indexOf('\01');
            int ctcpE = text.lastIndexOf('\01');
            if (ctcpS != -1 && ctcpE != -1) {
                for (String ctcpCommand : text.substring(ctcpS, ctcpE).split("\01"))
                    processCtcp(connection, sender, userUUID, targetChannels, ctcpCommand.indexOf('\134') == -1 ? ctcpCommand : ctcpDequote(ctcpCommand), tags);
                if (ctcpS == 0 && ctcpE == text.length() - 1)
                    return;
                text = text.substring(0, ctcpS) + text.substring(ctcpE + 1, text.length());
            }

            for (String channel : targetChannels) {
                if (channel.equals("*") && type == MessageInfo.MessageType.NOTICE) {
                    connection.getServerStatusData().addMessage(new StatusMessageInfo(sender.getServerName(),
                            new Date(), StatusMessageInfo.MessageType.NOTICE, params.get(1)));
                    continue;
                }

                ChannelData channelData = connection.getJoinedChannelData(channel);
                MessageInfo.Builder message = new MessageInfo.Builder(sender.toSenderInfo(userUUID, channelData), text, type);
                for (Capability cap : connection.getCapabilityManager().getEnabledCapabilities())
                    cap.processMessage(message, tags);
                connection.getMessageStorageApi().addMessage(channel, message.build(), null, null).get();
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private void processCtcp(ServerConnectionData connection, MessagePrefix sender, UUID userUUID, String[] targetChannels, String data, Map<String, String> tags) throws NoSuchChannelException, InterruptedException, ExecutionException {
        int iof = data.indexOf(' ');
        String command = iof == -1 ? data : data.substring(0, iof);
        String args = data.substring(iof + 1);
        if (command.equals("ACTION")) {
            for (String channel : targetChannels) {
                ChannelData channelData = connection.getJoinedChannelData(channel);
                MessageInfo.Builder message = new MessageInfo.Builder(sender.toSenderInfo(userUUID, channelData), args, MessageInfo.MessageType.ME);
                for (Capability cap : connection.getCapabilityManager().getEnabledCapabilities())
                    cap.processMessage(message, tags);
                connection.getMessageStorageApi().addMessage(channel, message.build(), null, null).get();
            }
        }
        // TODO: Implement other CTCP commands
    }

    // http://www.irchelp.org/protocol/ctcpspec.html

    private String lowDequote(String text) {
        StringBuilder outpBuilder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\020') {
                int cc = text.charAt(++i);
                switch (cc) {
                    case '0':
                        outpBuilder.append(0);
                        break;
                    case 'n':
                        outpBuilder.append('\n');
                        break;
                    case 'r':
                        outpBuilder.append('\r');
                        break;
                    case '\20':
                        outpBuilder.append('\20');
                        break;
                    default:
                        --i;
                }
            } else {
                outpBuilder.append(c);
            }
        }
        return outpBuilder.toString();
    }

    private String ctcpDequote(String text) {
        StringBuilder outpBuilder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\134') {
                if (text.charAt(i + 1) == 'a') {
                    outpBuilder.append('\01');
                    i++;
                }
            } else {
                outpBuilder.append(c);
            }
        }
        return outpBuilder.toString();
    }

}
