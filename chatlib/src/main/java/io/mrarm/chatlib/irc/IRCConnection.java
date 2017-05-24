package io.mrarm.chatlib.irc;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.user.SimpleUserInfoApi;
import io.mrarm.chatlib.util.SimpleRequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;
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

    private void sendCommand(String string, boolean flush) throws IOException {
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

    @Override
    public Future<Void> joinChannels(List<String> channels, ResponseCallback<Void> callback,
                                     ResponseErrorCallback errorCallback) {
        return executor.queue(() -> {
            StringBuilder cmd = new StringBuilder();
            cmd.append("JOIN ");
            boolean f = true;
            for (String channel : channels) {
                if (f)
                    f = false;
                else
                    cmd.append(",");
                cmd.append(channel);
            }
            sendCommand(cmd.toString(), true);
            return null;
        }, callback, errorCallback);
    }

    public void connectSync(IRCConnectionRequest request) throws IOException {
        Socket socket = new Socket(request.getServerIP(), request.getServerPort());
        socketInputStream = socket.getInputStream();
        socketOutputStream = socket.getOutputStream();
        // TODO: validate those params
        sendCommand("NICK " + request.getNickList().get(0), false);
        sendCommand("USER " + request.getUser() + " " + request.getUserMode() + " * " + request.getRealName(), true);
        System.out.println("Sent inital commands");

        Thread thread = new Thread(this::handleInput);
        thread.setName("IRC Connection Handler");
        thread.start();
    }


}
