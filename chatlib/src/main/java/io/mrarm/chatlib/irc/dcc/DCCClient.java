package io.mrarm.chatlib.irc.dcc;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class DCCClient implements Closeable {

    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
    private ByteBuffer ackBuffer = ByteBuffer.allocateDirect(4);

    private SocketChannel socket;
    private SocketAddress socketRemoteAddress;
    private FileChannel file;
    private SelectionKey selectionKey;
    private long offset;
    private long downloadedSize;
    private long expectedSize;
    private CloseListener closeListener;

    public DCCClient(FileChannel file, long offset, long size) {
        this.file = file;
        this.offset = offset;
        this.downloadedSize = offset;
        this.expectedSize = size;
    }

    public void setCloseListener(CloseListener listener) {
        closeListener = listener;
    }

    public void start(SocketChannel socket) throws IOException {
        this.socket = socket;
        this.socketRemoteAddress = socket.getRemoteAddress();
        this.socket.configureBlocking(false);
        this.file.position(offset);
        this.downloadedSize = offset;
        if (this.expectedSize > 0)
            this.file.truncate(this.expectedSize);
        try {
            selectionKey = DCCIOHandler.getInstance().register(socket, SelectionKey.OP_READ, (SelectionKey k) -> {
                if ((k.readyOps() & SelectionKey.OP_READ) != 0)
                    onRead();
            });
        } catch (ClosedChannelException ignored) {
        }
    }

    @Override
    public void close() {
        if (selectionKey != null)
            selectionKey.cancel();
        selectionKey = null;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        try {
            if (file != null)
                file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = null;

        if (closeListener != null)
            closeListener.onClosed(this);
    }

    public SocketAddress getRemoteAddress() {
        return socketRemoteAddress;
    }

    public long getExpectedSize() {
        return expectedSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    private int readSocket(ByteBuffer buffer) {
        if (socket == null)
            return -1;
        int r = -1;
        try {
            r = socket.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (r < 0) {
            close();
        } else {
            downloadedSize += r;
        }
        return r;
    }

    private void onRead() throws IOException {
        while (readSocket(buffer) > 0 || buffer.position() > 0) {
            buffer.flip();
            try {
                file.write(buffer);
            } finally {
                buffer.compact();
            }
        }
        writeDownloadState();
        if (expectedSize > 0 && downloadedSize >= expectedSize)
            close();
    }

    private void writeDownloadState() {
        if (socket == null)
            return;
        ackBuffer.clear();
        ackBuffer.putInt((int) downloadedSize);
        ackBuffer.flip();
        try {
            socket.write(ackBuffer);
        } catch (IOException e) {
        }
    }

    public interface CloseListener {

        void onClosed(DCCClient client);

    }

}
