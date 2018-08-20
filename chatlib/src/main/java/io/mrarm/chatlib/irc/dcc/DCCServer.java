package io.mrarm.chatlib.irc.dcc;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class DCCServer implements Closeable {

    private File file;
    private ServerSocketChannel serverSocket;
    private int socketLimit;
    private List<UploadSession> sessions = new ArrayList<>();

    public DCCServer(File file, int socketLimit) {
        this.file = file;
        this.socketLimit = socketLimit;
    }

    public int createServerSocket() throws IOException {
        if (serverSocket == null) {
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(0));
            serverSocket.configureBlocking(false);
            DCCIOHandler.getInstance().addServer(this, serverSocket);
        }
        return serverSocket.socket().getLocalPort();
    }

    public int getPort() {
        if (serverSocket == null)
            return -1;
        return serverSocket.socket().getLocalPort();
    }

    @Override
    public void close() throws IOException {
        if (serverSocket != null)
            serverSocket.close();
        for (UploadSession session : sessions)
            session.close();
    }

    void doAccept() throws IOException {
        if (socketLimit != -1 && sessions.size() >= socketLimit)
            return;
        SocketChannel socket = serverSocket.accept();
        if (socket == null)
            return;
        new UploadSession(file, socket);
    }


    class UploadSession implements Closeable {

        private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        private FileChannel file;
        private SelectionKey selectionKey;
        SocketChannel socket;

        UploadSession(File file, SocketChannel socket) throws IOException {
            try {
                this.file = new FileInputStream(file).getChannel();
                this.socket = socket;
                socket.configureBlocking(false);
                selectionKey = DCCIOHandler.getInstance().addUploadSession(this);
                sessions.add(this);
            } catch (IOException e) {
                close();
                throw e;
            }
        }

        @Override
        public void close() {
            sessions.remove(this);
            if (selectionKey != null)
                selectionKey.cancel();
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException ignored) {
            }
            try {
                if (file != null)
                    file.close();
            } catch (IOException ignored) {
            }
        }

        void doRead() throws IOException {
            while (socket.read(readBuffer) > 0); // ignore all the input
        }

        void doWrite() throws IOException {
            int r;
            while ((r = file.read(buffer)) > 0 || buffer.position() > 0) {
                buffer.flip();
                socket.write(buffer);
                buffer.compact();
            }
        }

    }

}
