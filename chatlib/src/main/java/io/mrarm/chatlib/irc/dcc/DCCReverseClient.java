package io.mrarm.chatlib.irc.dcc;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class DCCReverseClient implements Closeable {

    private FileChannel file;
    private ServerSocketChannel serverSocket;
    private DCCClient client;
    private long offset;
    private long size;
    private StateListener stateListener;

    public DCCReverseClient(FileChannel file, long offset, long size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public synchronized void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public synchronized int createServerSocket() throws IOException {
        if (serverSocket == null) {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(0));
            serverSocket.configureBlocking(false);
            DCCIOHandler.getInstance().register(serverSocket, SelectionKey.OP_ACCEPT, (SelectionKey k) -> {
                if (k.isAcceptable())
                    doAccept();
            });
        }
        return serverSocket.socket().getLocalPort();
    }

    public synchronized int getPort() {
        if (serverSocket == null)
            return -1;
        return serverSocket.socket().getLocalPort();
    }

    public synchronized DCCClient getClient() {
        return client;
    }

    @Override
    public synchronized void close() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException ignored) {
        }
        serverSocket = null;
        try {
            if (file != null)
                file.close();
        } catch (IOException ignored) {
        }
        file = null;
        if (client != null)
            client.close();
        client = null;

        if (stateListener != null)
            stateListener.onClosed(this);
    }

    private synchronized void doAccept() throws IOException {
        SocketChannel socket = serverSocket.accept();
        if (socket == null)
            return;
        client = new DCCClient(file, offset, size);
        client.setCloseListener((DCCClient client) -> {
            if (stateListener != null)
                stateListener.onClosed(this);
        });
        client.start(socket);
        file = null; // we no longer own it
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        serverSocket = null;
        if (stateListener != null)
            stateListener.onClientConnected(this, client);
    }

    public interface StateListener {

        void onClosed(DCCReverseClient reverseClient);

        void onClientConnected(DCCReverseClient reverseClient, DCCClient client);

    }

}
