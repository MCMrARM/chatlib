package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.irc.dcc.DCCClientManager;
import io.mrarm.chatlib.irc.dcc.DCCServerManager;
import io.mrarm.chatlib.irc.dcc.DCCUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MessageCommandHandler implements CommandHandler {

    private long ctcpLastReplySeconds = 0L;
    private int ctcpSecondReplyCount = 0;
    private String ctcpVersionReply = "Chatlib:unknown:unknown";
    private DCCServerManager dccServerManager;
    private DCCClientManager dccClientManager;

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { "PRIVMSG", "NOTICE" };
    }

    public void setCtcpVersionReply(String str) {
        ctcpVersionReply = str;
    }

    public void setCtcpVersionReply(String clientName, String clientVersion, String system) {
        setCtcpVersionReply(clientName + ":" + clientVersion + ":" + system);
    }

    public void setDCCServerManager(DCCServerManager dccServerManager) {
        this.dccServerManager = dccServerManager;
    }

    public void setDCCClientManager(DCCClientManager dccClientManager) {
        this.dccClientManager = dccClientManager;
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        try {
            MessageInfo.MessageType type = (command.equals("NOTICE") ? MessageInfo.MessageType.NOTICE :
                    MessageInfo.MessageType.NORMAL);
            UUID userUUID = null;
            if (sender != null)
                userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(), sender.getHost(),
                        null, null).get();
            String[] targetChannels = CommandHandler.getParamWithCheck(params, 0).split(",");

            String text = CommandHandler.getParamWithCheck(params, 1);
            if (text.indexOf('\20') != -1)
                text = lowDequote(text);
            int ctcpS = text.indexOf('\01');
            int ctcpE = text.lastIndexOf('\01');
            if (ctcpS != -1 && ctcpE != -1 && sender != null) {
                for (String ctcpCommand : text.substring(ctcpS, ctcpE).split("\01"))
                    processCtcp(connection, sender, userUUID, targetChannels, ctcpCommand.indexOf('\134') == -1 ? ctcpCommand : ctcpDequote(ctcpCommand), type == MessageInfo.MessageType.NOTICE, tags);
                if (ctcpS == 0 && ctcpE == text.length() - 1)
                    return;
                text = text.substring(0, ctcpS) + text.substring(ctcpE + 1, text.length());
            }

            for (String channel : targetChannels) {
                ChannelData channelData = null;
                try {
                    channelData = connection.getJoinedChannelData(channel);
                } catch (NoSuchChannelException ignored) {
                }
                if (sender == null || (channelData == null && sender.getUser() == null && sender.getHost() == null)) {
                    connection.getServerStatusData().addMessage(new StatusMessageInfo(sender != null ?
                            sender.getServerName() : null, new Date(), StatusMessageInfo.MessageType.NOTICE, text));
                    continue;
                }

                if (channel.equalsIgnoreCase(connection.getUserNick()) || channelData == null) {
                    channelData = getChannelData(connection, sender, channel);
                    if (channelData == null)
                        continue;
                }
                channelData.addMessage(new MessageInfo.Builder(sender.toSenderInfo(userUUID, channelData), text, type), tags);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void processCtcp(ServerConnectionData connection, MessagePrefix sender, UUID userUUID, String[] targetChannels, String data, boolean notice, Map<String, String> tags) throws InterruptedException, ExecutionException {
        int iof = data.indexOf(' ');
        String command = iof == -1 ? data : data.substring(0, iof);
        String args = data.substring(iof + 1);
        if (command.equals("ACTION")) {
            for (String channel : targetChannels) {
                ChannelData channelData = getChannelData(connection, sender, channel);
                if (channelData == null)
                    continue;
                channelData.addMessage(new MessageInfo.Builder(sender.toSenderInfo(userUUID, channelData), args, MessageInfo.MessageType.ME), tags);
            }
        } else if (command.equals("PING") && !notice) {
            if (!rateLimitCtcpCommand() || args.contains("\r") || args.contains("\n") || args.length() > 32)
                return;
            connection.getServerStatusData().addMessage(new StatusMessageInfo(sender.getNick(), new Date(), StatusMessageInfo.MessageType.CTCP_PING, null));
            connection.getApi().sendNotice(sender.getNick(), "\01PING " + args + "\01", null, null);
        } else if (command.equals("VERSION") && !notice) {
            if (!rateLimitCtcpCommand())
                return;
            connection.getServerStatusData().addMessage(new StatusMessageInfo(sender.getNick(), new Date(), StatusMessageInfo.MessageType.CTCP_VERSION, null));
            connection.getApi().sendNotice(sender.getNick(), "\01VERSION " + ctcpVersionReply + "\01", null, null);
        } else if (command.equals("DCC")) {
            if (args.startsWith("RESUME ") && dccServerManager != null && rateLimitCtcpCommand()) {
                args = args.substring(7);
                int filenameLen = DCCUtils.getFilenameLength(args);
                String filename = args.substring(0, filenameLen);
                String[] otherArgs = args.substring(filenameLen + (args.charAt(filenameLen) == ' ' ? 1 : 0)).split(" ");
                if (dccServerManager.continueUpload(connection, sender.getNick(), filename,
                        Integer.parseInt(otherArgs[0]), Long.parseLong(otherArgs[1]))) {
                    connection.getApi().sendMessage(sender.getNick(), "\01DCC ACCEPT " + filename + " " +
                            otherArgs[0] + " " + otherArgs[1] + "\01", null, null);
                }
            }
            if (args.startsWith("SEND ") && dccClientManager != null) {
                args = args.substring(5);
                int filenameLen = DCCUtils.getFilenameLength(args);
                String filename = args.substring(0, filenameLen);
                String[] otherArgs = args.substring(filenameLen + (args.charAt(filenameLen) == ' ' ? 1 : 0)).split(" ");
                String ip = DCCUtils.convertIPFromCommand(otherArgs[0]);
                int port = Integer.parseInt(otherArgs[1]);
                long size = -1;
                try {
                    size = Long.parseLong(otherArgs[2]);
                } catch (Exception ignored) { // NumberFormatException or NPE
                }
                if (otherArgs.length > 3 && port == 0) { // Reverse DCC request
                    int reverseId = Integer.parseInt(otherArgs[3]);
                    if (dccClientManager != null)
                        dccClientManager.onFileOfferedUsingReverse(connection, sender, filename, size, reverseId);
                    return;
                }
                if (otherArgs.length > 3) { // Reverse DCC response
                    int reverseId = Integer.parseInt(otherArgs[3]);
                    if (dccServerManager != null) // no need to rate limit, as we limit the count of uploads in that part of code anyways
                        dccServerManager.handleReverseUploadResponse(connection, sender.getNick(), filename, reverseId,
                                ip, port);
                    return;
                }

                dccClientManager.onFileOffered(connection, sender, filename, ip, port, size);
            }
        }
        // TODO: Implement other CTCP commands
    }

    private boolean rateLimitCtcpCommand() {
        long t = System.currentTimeMillis() / 1000L;
        if (t != ctcpLastReplySeconds) {
            ctcpLastReplySeconds = t;
            ctcpSecondReplyCount = 1;
            return true;
        }
        return (++ctcpSecondReplyCount <= 3);
    }

    private ChannelData getChannelData(ServerConnectionData connection, MessagePrefix sender, String channel) {
        boolean isDirectMessage = (channel.equalsIgnoreCase(connection.getUserNick()) ||
                channel.equalsIgnoreCase(sender.getNick()));
        if (isDirectMessage)
            channel = sender.getNick();
        try {
            return connection.getJoinedChannelData(channel);
        } catch (NoSuchChannelException exception) {
            if (isDirectMessage || (channel.length() > 0 &&
                    !connection.getSupportList().getSupportedChannelTypes().contains(channel.charAt(0)))) {
                connection.onChannelJoined(channel);
                try {
                    return connection.getJoinedChannelData(channel);
                } catch (NoSuchChannelException e) {
                    return null;
                }
            }
            return null;
        }
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
                if (i + 1 < text.length() && text.charAt(i + 1) == 'a') {
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
