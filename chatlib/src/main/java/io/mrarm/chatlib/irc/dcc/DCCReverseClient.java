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

    public DCCReverseClient(FileChannel file, long offset, long size) {
        this.file = file;
        this.offset = offset;
        this.size = size;
    }

    public int createServerSocket() throws IOException {
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

    public int getPort() {
        if (serverSocket == null)
            return -1;
        return serverSocket.socket().getLocalPort();
    }

    public DCCClient getClient() {
        return client;
    }

    @Override
    public void close() {
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
    }

    private void doAccept() throws IOException {
        SocketChannel socket = serverSocket.accept();
        if (socket == null)
            return;
        client = new DCCClient(socket, file, offset, size);
        file = null; // we no longer own it
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
        serverSocket = null;
    }

}
