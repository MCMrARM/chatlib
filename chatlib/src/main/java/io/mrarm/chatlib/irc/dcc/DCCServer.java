package io.mrarm.chatlib.irc.dcc;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class DCCServer implements Closeable {

    private FileChannelFactory fileFactory;
    private ServerSocketChannel serverSocket;
    private int socketLimit;
    private List<UploadSession> sessions = new ArrayList<>();
    private long offset = 0;

    public DCCServer(FileChannelFactory fileFactory, int socketLimit) {
        this.fileFactory = fileFactory;
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

    public void setFileOffset(long offset) {
        this.offset = offset;
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
        System.out.println("Accepted DCC connection from: " + socket.getRemoteAddress().toString());
        new UploadSession(fileFactory.open(), socket, offset);
    }


    class UploadSession implements Closeable {

        private ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        private FileChannel file;
        private SelectionKey selectionKey;
        private long totalSize;
        SocketChannel socket;

        UploadSession(FileChannel file, SocketChannel socket, long startPos) throws IOException {
            try {
                totalSize += startPos;
                if (startPos != 0)
                    file.position(startPos);
                this.file = file;
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
            System.out.println("Closed DCC connection");
            sessions.remove(this);
            if (selectionKey != null)
                DCCIOHandler.getInstance().unregister(selectionKey);
            selectionKey = null;
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
            try {
                if (file != null)
                    file.close();
            } catch (IOException ignored) {
            }
            file = null;
        }

        void doRead() throws IOException {
            while (socket.read(readBuffer) > 0) {
                readBuffer.flip();
                while (readBuffer.remaining() >= 4) {
                    long cnt = readBuffer.getInt() & 0xffffffffL;
                    if (file == null && cnt >= totalSize) {
                        close();
                        return;
                    }
                }
                readBuffer.compact();
            }
        }

        private int readFile(ByteBuffer buffer) {
            if (file == null)
                return -1;
            int r = -1;
            try {
                r = file.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (r < 0) {
                try {
                    if (file != null)
                        file.close();
                } catch (IOException ignored) {
                }
                file = null;
            } else {
                totalSize += r;
            }
            return r;
        }

        void doWrite() throws IOException {
            while (readFile(buffer) > 0 || buffer.position() > 0) {
                buffer.flip();
                socket.write(buffer);
                buffer.compact();
            }
        }

    }

    public interface FileChannelFactory {

        FileChannel open() throws IOException;

    };

}
