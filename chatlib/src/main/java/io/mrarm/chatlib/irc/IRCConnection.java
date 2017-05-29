package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class IRCConnection extends ServerConnectionApi {

    private Socket socket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private MessageHandler inputHandler;
    private CommandHandlerList commandHandlerList;

    private SimpleRequestExecutor executor = new SimpleRequestExecutor();

    public IRCConnection() {
        super(new ServerConnectionData());
        commandHandlerList = new CommandHandlerList();
        commandHandlerList.addDefaultHandlers();
        inputHandler = new MessageHandler(getServerConnectionData(), commandHandlerList);
        getServerConnectionData().setUserInfoApi(new SimpleUserInfoApi());
    }

    private void sendCommandRaw(String string, boolean flush) throws IOException {
        synchronized (socketOutputStream) {
            try {
                byte[] data = (string + '\n').getBytes("UTF-8");
                if (data.length > 512)
                    throw new IOException("Too long message");
                socketOutputStream.write(data);
                System.out.println("Sent: " + string);
                if (flush)
                    socketOutputStream.flush();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendCommand(boolean flush, String command, boolean isLastArgFullLine, String... args) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append(command); // TODO: validate
        builder.append(' ');
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                builder.append(' ');
            if (i == args.length - 1 && isLastArgFullLine) {
                builder.append(':');
                // TODO: validate
            } else {
                // TODO: validate
            }
            builder.append(args[i]);
        }
        sendCommandRaw(builder.toString(), flush);
    }

    private void sendCommand(String command, boolean isLastArgFullLine, String... args) throws IOException {
        sendCommand(true, command, isLastArgFullLine, args);
    }

    private String readCommand() throws IOException {
        byte[] buf = new byte[512];
        int i, v;
        for (i = 0; i < 512; i++) {
            v = socketInputStream.read();
            if (v == -1)
                throw new IOException("read() returned -1");
            if (v == '\n' || v == '\r') {
                if (i == 0) {
                    --i;
                    continue;
                }
                break;
            }
            buf[i] = (byte) v;
        }
        return new String(buf, 0, i, "UTF-8");
    }

    private void handleInput() {
        try {
            while (true) {
                String command = readCommand();
                System.out.println("Got: " + command);
                try {
                    inputHandler.handleLine(command);
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Future<Void> connect(IRCConnectionRequest request, ResponseCallback<Void> callback,
                                ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            connectSync(request);
            return null;
        }, callback, errorCallback);
    }

    // TODO: validate params

    @Override
    public Future<Void> joinChannels(List<String> channels, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            StringBuilder cmd = new StringBuilder();
            boolean f = true;
            for (String channel : channels) {
                if (f)
                    f = false;
                else
                    cmd.append(",");
                cmd.append(channel);
            }
            sendCommand("JOIN", true, cmd.toString());
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> sendMessage(String channel, String message, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            try {
                UUID userUUID = getUserInfoApi().resolveUser(getServerConnectionData().getUserNick(), null, null,
                        null, null).get();
                ChannelData channelData = getChannelData(channel);
                ChannelData.Member memberInfo = channelData.getMember(userUUID);
                MessageSenderInfo sender = new MessageSenderInfo(getServerConnectionData().getUserNick(), null, null,
                        memberInfo != null ? memberInfo.getNickPrefixes() : null, userUUID);
                channelData.addMessage(new MessageInfo(sender, new Date(), message, MessageInfo.MessageType.NORMAL));
            } catch (Exception ignored) {
                // it failed, but we don't really care - the message might have been sent to a channel which we have not
                // joined, which is perfectly valid but will cause the code above to raise an exception
            }

            sendCommand("PRIVMSG", true, channel, message);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public void sendPong(String text) {
        try {
            sendCommand("PONG", true, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSync(IRCConnectionRequest request) throws IOException {
        if (request.isUsingSSL()) {
            socket = request.getSSLSocketFactory().createSocket(request.getServerIP(), request.getServerPort());
            HostnameVerifier hostnameVerifier = request.getSSLHostnameVerifier();
            if (hostnameVerifier != null) {
                SSLSocket sslSocket = (SSLSocket) socket;
                sslSocket.setUseClientMode(true);
                sslSocket.startHandshake();
                if (!hostnameVerifier.verify(request.getServerIP(), sslSocket.getSession()))
                    throw new IOException("Failed to verify hostname: " + request.getServerIP());
            }
        } else {
            socket = new Socket(request.getServerIP(), request.getServerPort());
        }
        socketInputStream = socket.getInputStream();
        socketOutputStream = socket.getOutputStream();
        sendCommand(false, "NICK", false, request.getNickList().get(0));
        getServerConnectionData().setUserNick(request.getNickList().get(0));
        sendCommand("USER", true, request.getUser(), String.valueOf(request.getUserMode()), "*", request.getRealName());
        System.out.println("Sent inital commands");

        Thread thread = new Thread(this::handleInput);
        thread.setName("IRC Connection Handler");
        thread.start();

        waitForMotd();
    }

}
