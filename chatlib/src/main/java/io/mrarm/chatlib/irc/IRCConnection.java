package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.ChatApiException;
import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.dto.WhoisInfo;
import io.mrarm.chatlib.irc.handlers.MessageCommandHandler;
import io.mrarm.chatlib.irc.handlers.NickCommandHandler;
import io.mrarm.chatlib.irc.handlers.WhoisCommandHandler;
import io.mrarm.chatlib.message.SimpleMessageStorageApi;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.util.SettableFuture;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Future;

public class IRCConnection extends ServerConnectionApi {

    private static final MessageCommandHandler selfMessageHandler = new MessageCommandHandler();

    private Socket socket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;
    private MessageHandler inputHandler;
    private ResponseCallback<Void> connectCallback;
    private ResponseErrorCallback connectErrorCallback;
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
        int l = args != null ? args.length : 0;
        for (int i = 0; i < l; i++) {
            if (i > 0)
                builder.append(' ');
            if (i == l - 1 && isLastArgFullLine) {
                builder.append(':');
                // TODO: validate
            } else {
                // TODO: validate
            }
            builder.append(args[i]);
        }
        sendCommandRaw(builder.toString(), flush);
    }

    @Override
    public void sendCommand(String command, boolean isLastArgFullLine, String... args) throws IOException {
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
                    getServerConnectionData().getServerStatusData().addMessage(new StatusMessageInfo(
                            null, new Date(), StatusMessageInfo.MessageType.UNHANDLED_MESSAGE, command));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (connectErrorCallback != null)
                connectErrorCallback.onError(e);
            getServerConnectionData().addLocalMessageToAllChannels(new MessageInfo(null, new Date(), null, MessageInfo.MessageType.DISCONNECT_WARNING));
            getServerConnectionData().getServerStatusData().addMessage(new StatusMessageInfo(null, new Date(), StatusMessageInfo.MessageType.DISCONNECT_WARNING, null));
            getServerConnectionData().getCommandHandlerList().notifyDisconnected();
            synchronized (disconnectListeners) {
                for (DisconnectListener listener : disconnectListeners) {
                    listener.onDisconnected(this, e);
                }
            }
        }
    }

    public Future<Void> connect(IRCConnectionRequest request, ResponseCallback<Void> callback,
                                ResponseErrorCallback errorCallback) {
        SettableFuture<Void> f = new SettableFuture<>();
        executor.queue(() -> {
            try {
                synchronized (this) {
                    if (socket != null)
                        throw new RuntimeException("Already connected");
                    connectCallback = (Void v) -> {
                        f.set(v);
                        callback.onResponse(v);
                    };
                    connectErrorCallback = (Exception e) -> {
                        f.setExecutionException(e);
                        errorCallback.onError(e);
                    };
                    connectSync(request);
                }
            } catch (Exception exception) {
                errorCallback.onError(exception);
                f.setExecutionException(exception);
            }
        });
        return f;
    }

    public void disconnect(boolean cleanly) {
        if (socket != null) {
            if (cleanly) {
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
    public Future<Void> quit(String message, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            sendCommand("QUIT", true, message);
            disconnect(true);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<WhoisInfo> sendWhois(String nick, ResponseCallback<WhoisInfo> callback, ResponseErrorCallback errorCallback) {
        SettableFuture<WhoisInfo> ret = new SettableFuture<>();
        executor.queue(ret, () -> {
            if (getServerConnectionData().getCommandHandlerList().getHandler(WhoisCommandHandler.class).onRequested(
                    nick, (WhoisInfo info) -> {
                        executor.queue(() -> {
                            ret.set(info);
                            if (callback != null)
                                callback.onResponse(info);
                        });
                    }, (String s, int i, String e) -> {
                        NumericReplyException exception = new NumericReplyException(i, e);
                        if (errorCallback != null)
                            errorCallback.onError(exception);
                        ret.setExecutionException(exception);
                    }))
                sendCommand("WHOIS", false, nick);
        }, errorCallback);
        return ret;
    }

    @Override
    public Future<Void> joinChannels(List<String> channels, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            StringBuilder cmd = new StringBuilder();
            for (String channel : channels) {
                if (channel.length() == 0)
                    continue;
                if (!getServerConnectionData().getSupportList().getSupportedChannelTypes().contains(channel.charAt(0))) {
                    getServerConnectionData().onChannelJoined(channel);
                    continue;
                }
                if (cmd.length() > 0)
                    cmd.append(",");
                cmd.append(channel);
            }
            if (cmd.length() > 0)
                sendCommand("JOIN", true, cmd.toString());
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> leaveChannel(String channel, String reason, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            if (channel.length() == 0)
                return null;
            if (!getServerConnectionData().getSupportList().getSupportedChannelTypes().contains(channel.charAt(0)))
                getServerConnectionData().onChannelLeft(channel);
            else
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
    public Future<Void> sendCommand(String command, boolean isLastArgFullLine, String[] args,
                                    ResponseCallback<Void> callback, ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            sendCommand(command, isLastArgFullLine, args);
            return null;
        }, callback, errorCallback);
    }

    private Future<Void> sendMessage(String channel, String message, boolean notice, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            String cmd = notice ? "NOTICE" : "PRIVMSG";
            try {
                List<String> params = new ArrayList<>();
                params.add(channel);
                params.add(message);
                selfMessageHandler.handle(getServerConnectionData(), new MessagePrefix(getServerConnectionData().getUserNick()), cmd, params, null);
            } catch (Exception ignored) {
                // it failed, but we don't really care - the message might have been sent to a channel which we have not
                // joined, which is perfectly valid but will cause the code above to raise an exception
            }
            sendCommand(cmd, true, channel, message);
            return null;
        }, callback, errorCallback);
    }

    @Override
    public Future<Void> sendMessage(String channel, String message, ResponseCallback<Void> callback,
                                    ResponseErrorCallback errorCallback) {
        return sendMessage(channel, message, false, callback, errorCallback);
    }

    @Override
    public Future<Void> sendNotice(String channel, String message, ResponseCallback<Void> callback,
                                   ResponseErrorCallback errorCallback) {
        return sendMessage(channel, message, true, callback, errorCallback);
    }

    private void connectSync(IRCConnectionRequest request) throws IOException {
        getServerConnectionData().reset();
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
        connectRequestNick(request.getNickList(), 0);
        sendCommand("USER", true, request.getUser(), String.valueOf(request.getUserMode()), "*", request.getRealName());
        System.out.println("Sent inital commands");

        Thread thread = new Thread(this::handleInput);
        thread.setName("IRC Connection Handler");
        thread.start();
    }

    @Override
    public void notifyMotdReceived() {
        super.notifyMotdReceived();
        getServerConnectionData().getCommandHandlerList().getHandler(NickCommandHandler.class).cancel(
                getServerConnectionData().getUserNick());
        if (connectCallback != null)
            connectCallback.onResponse(null);
        connectCallback = null;
        connectErrorCallback = null;
    }

    private void connectRequestNick(List<String> nickList, int index) throws IOException {
        getServerConnectionData().setUserNick(nickList.get(index));
        getServerConnectionData().getCommandHandlerList().getHandler(NickCommandHandler.class).onRequested(
                nickList.get(0), null, (String n, int i, String err) -> {
                    if (i == NickCommandHandler.ERR_NICKNAMEINUSE) {
                        // Try next nickname
                        if (index + 1 >= nickList.size()) {
                            if (connectErrorCallback != null)
                                connectErrorCallback.onError(new ChatApiException("No available nickname"));
                            return;
                        }
                        try {
                            connectRequestNick(nickList, index + 1);
                        } catch (IOException e) {
                            if (connectErrorCallback != null)
                                connectErrorCallback.onError(new ChatApiException("Failed to request nickname"));
                        }
                    }
                });
        sendCommand(false, "NICK", false, nickList.get(index));
    }

    public interface DisconnectListener {
        void onDisconnected(IRCConnection connection, Exception reason);
    }

}
