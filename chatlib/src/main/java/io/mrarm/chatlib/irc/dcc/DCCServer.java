package io.mrarm.chatlib.irc.dcc;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DCCServer implements Closeable {

    private final FileChannelFactory fileFactory;
    private ServerSocketChannel serverSocket;
    private final List<SessionListener> sessionListeners = new ArrayList<>();
    private final int socketLimit;
    private final List<UploadSession> sessions = new ArrayList<>();
    private long offset = 0;

    public DCCServer(FileChannelFactory fileFactory, int socketLimit) {
        this.fileFactory = fileFactory;
        this.socketLimit = socketLimit;
    }

    public void addSessionListener(SessionListener listener) {
        synchronized (sessionListeners) {
            sessionListeners.add(listener);
        }
    }

    public void removeSessionListener(SessionListener listener) {
        synchronized (sessionListeners) {
            sessionListeners.remove(listener);
        }
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

    public void setFileOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public void close() throws IOException {
        if (serverSocket != null)
            serverSocket.close();
        synchronized (sessions) {
            for (UploadSession session : sessions)
                session.close();
        }
    }

    void doAccept() throws IOException {
        if (socketLimit != -1 && sessions.size() >= socketLimit)
            return;
        SocketChannel socket = serverSocket.accept();
        if (socket == null)
            return;
        System.out.println("Accepted DCC connection from: " + socket.socket().getRemoteSocketAddress().toString());
        new UploadSession(fileFactory.open(), socket, offset);
    }

    public boolean startReverseUpload(String ip, int port) throws IOException {
        if (socketLimit != -1 && sessions.size() >= socketLimit)
            return false;
        System.out.println("Starting reverse DCC upload to: " + ip + ":" + port);
        SocketChannel socket = SocketChannel.open(new InetSocketAddress(ip, port));
        if (socket == null)
            return false;
        new UploadSession(fileFactory.open(), socket, offset);
        return true;
    }


    public class UploadSession implements Closeable {

        private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
        private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
        private final FileChannel file;
        private SelectionKey selectionKey;
        private final AtomicLong ackedSize = new AtomicLong();
        private final long totalSize;
        private final SocketChannel socket;
        private final SocketAddress remoteAddress;

        private DCCIOHandler.SelectHandler selectionKeyHandler = (SelectionKey k) -> {
            if ((k.readyOps() & SelectionKey.OP_READ) != 0)
                doRead();
            if (!k.isValid()) {
                close();
                return;
            }
            if (k.isWritable())
                doWrite();
        };

        UploadSession(FileChannel file, SocketChannel socket, long startPos) throws IOException {
            try {
                totalSize = file.size();
                ackedSize.addAndGet(startPos);
                if (startPos != 0)
                    file.position(startPos);

                this.file = file;
                this.socket = socket;
                socket.configureBlocking(false);
                remoteAddress = socket.socket().getRemoteSocketAddress();
                selectionKey = DCCIOHandler.getInstance().register(socket,
                        SelectionKey.OP_READ | SelectionKey.OP_WRITE, selectionKeyHandler);
            } catch (IOException e) {
                close();
                throw e;
            }

            synchronized (sessions) {
                sessions.add(this);
            }
            synchronized (sessionListeners) {
                for (SessionListener listener : sessionListeners)
                    listener.onSessionCreated(DCCServer.this, this);
            }
        }

        public SocketAddress getRemoteAddress() {
            return remoteAddress;
        }

        public DCCServer getServer() {
            return DCCServer.this;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getAcknowledgedSize() {
            return ackedSize.get();
        }

        @Override
        public void close() {
            System.out.println("Closed DCC connection");
            synchronized (sessions) {
                sessions.remove(this);
            }
            synchronized (this) {
                if (selectionKey != null)
                    DCCIOHandler.getInstance().unregister(selectionKey);
                selectionKey = null;
            }
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

            synchronized (sessionListeners) {
                for (SessionListener listener : sessionListeners)
                    listener.onSessionDestroyed(DCCServer.this, this);
            }
        }

        private void doRead() throws IOException {
            int r;
            while ((r = socket.read(readBuffer)) > 0) {
                readBuffer.flip();
                while (readBuffer.remaining() >= 4) {
                    long cnt = readBuffer.getInt() & 0xffffffffL;
                    ackedSize.set(cnt);
                    if (file == null && cnt >= totalSize) {
                        close();
                        return;
                    }
                }
                readBuffer.compact();
            }
            if (r < 0)
                close();
        }

        private int readFile(ByteBuffer buffer) {
            if (!file.isOpen())
                return -1;
            int r = -1;
            try {
                r = file.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (r < 0) {
                try {
                    file.close();
                } catch (IOException ignored) {
                }
            }
            return r;
        }

        private void doWrite() throws IOException {
            while (readFile(buffer) > 0 || buffer.position() > 0) {
                buffer.flip();
                try {
                    if (socket.write(buffer) <= 0)
                        return;
                } finally {
                    buffer.compact();
                }
            }
        }

    }

    public interface FileChannelFactory {

        FileChannel open() throws IOException;

    }

    public interface SessionListener {

        void onSessionCreated(DCCServer server, UploadSession session);

        void onSessionDestroyed(DCCServer server, UploadSession session);

    }

}
