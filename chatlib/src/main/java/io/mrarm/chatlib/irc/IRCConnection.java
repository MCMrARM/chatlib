package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.handlers.MessageCommandHandler;
import io.mrarm.chatlib.message.SimpleMessageStorageApi;
import io.mrarm.chatlib.message.WritableMessageStorageApi;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

public class IRCConnection extends ServerConnectionApi {

    private static final MessageCommandHandler selfMessageHandler = new MessageCommandHandler();

    private Socket socket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private MessageHandler inputHandler;
    private final List<DisconnectListener> disconnectListeners = new ArrayList<>();

    private SimpleRequestExecutor executor = new SimpleRequestExecutor();

    public IRCConnection() {
        super(new ServerConnectionData());
        inputHandler = new MessageHandler(getServerConnectionData());
        getServerConnectionData().setUserInfoApi(new SimpleUserInfoApi());
        getServerConnectionData().setMessageStorageApi(new SimpleMessageStorageApi());
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
        byte[] buf = new byte[1024];
        int i, v;
        for (i = 0; i < 1024; i++) {
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
            if (!hasReceivedMotd())
                notifyMotdReceiveFailed();
            getServerConnectionData().addLocalMessageToAllChannels(new MessageInfo(null, new Date(), null, MessageInfo.MessageType.DISCONNECT_WARNING));
            getServerConnectionData().getServerStatusData().addMessage(new StatusMessageInfo(null, new Date(), StatusMessageInfo.MessageType.DISCONNECT_WARNING, null));
            synchronized (disconnectListeners) {
                for (DisconnectListener listener : disconnectListeners) {
                    listener.onDisconnected(this, e);
                }
            }
        }
    }

    public Future<Void> connect(IRCConnectionRequest request, ResponseCallback<Void> callback,
                                ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            connectSync(request);
            return null;
        }, callback, errorCallback);
    }

    public void disconnect(boolean cleanly) {
        if (socket != null) {
            if (cleanly && hasReceivedMotd()) {
                try {
                    socketInputStream.close();
                    socketOutputStream.close();
                } catch (IOException ignored) {
                }
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Future<Void> disconnect(ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            disconnect(true);
            return null;
        }, callback, errorCallback);
    }

    public void addDisconnectListener(DisconnectListener listener) {
        synchronized (disconnectListeners) {
            disconnectListeners.add(listener);
        }
    }

    public void removeDisconnectListener(DisconnectListener listener) {
        synchronized (disconnectListeners) {
            disconnectListeners.remove(listener);
        }
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
    public Future<Void> leaveChannel(String channel, String reason, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            sendCommand("PART", true, channel, reason);
            return null;
        }, callback, errorCallback);
    }

    public Future<Void> sendCommandRaw(String command, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            sendCommandRaw(command, true);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> sendMessage(String channel, String message, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            try {
                List<String> params = new ArrayList<>();
                params.add(channel);
                params.add(message);
                selfMessageHandler.handle(getServerConnectionData(), new MessagePrefix(getServerConnectionData().getUserNick()), "PRIVMSG", params, null);
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

    @Override
    public void requestCapabilities(List<String> capabilities) {
        try {
            // TODO: Somehow handle a situation where the resulting string is larger than the maximal allowed message
            // length (specs don't really mention what should be done in this case ?)
            StringBuilder capsBuilder = new StringBuilder();
            boolean f = true;
            for (String cap : capabilities) {
                if (f)
                    f = false;
                else
                    capsBuilder.append(' ');
                capsBuilder.append(cap);
            }
            sendCommand("CAP", true, "REQ", capsBuilder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void endCapabilityNegotiation() {
        try {
            sendCommand("CAP", false, "END");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSync(IRCConnectionRequest request) throws IOException {
        resetMotdStatus();
        getServerConnectionData().getCapabilityManager().reset();
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
        sendCommand(false, "CAP", false, "LS", "302");
        if (request.getServerPass() != null)
            sendCommand(false, "PASS", request.getServerPass().contains(" "), request.getServerPass());
        sendCommand(false, "NICK", false, request.getNickList().get(0));
        getServerConnectionData().setUserNick(request.getNickList().get(0));
        sendCommand("USER", true, request.getUser(), String.valueOf(request.getUserMode()), "*", request.getRealName());
        System.out.println("Sent inital commands");

        Thread thread = new Thread(this::handleInput);
        thread.setName("IRC Connection Handler");
        thread.start();

        if (!waitForMotd())
            throw new IOException("Failed to receive MOTD");
    }

    public interface DisconnectListener {
        void onDisconnected(IRCConnection connection, Exception reason);
    }

}
