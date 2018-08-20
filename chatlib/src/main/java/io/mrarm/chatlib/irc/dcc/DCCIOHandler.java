package io.mrarm.chatlib.irc.dcc;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class DCCIOHandler extends Thread {

    private Selector selector;

    private static final DCCIOHandler instanceSingleton = new DCCIOHandler();
    private boolean running = false;

    public static DCCIOHandler getInstance() {
        return instanceSingleton;
    }

    public DCCIOHandler() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    SelectionKey addServer(DCCServer server, ServerSocketChannel channel) throws ClosedChannelException {
        SelectionKey ret = channel.register(selector, SelectionKey.OP_ACCEPT, server);
        startIfNeeded();
        selector.wakeup();
        return ret;
    }

    SelectionKey addUploadSession(DCCServer.UploadSession session) throws ClosedChannelException {
        SelectionKey ret = session.socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, session);
        startIfNeeded();
        selector.wakeup();
        return ret;
    }

    private void handleSelectionKey(SelectionKey k) throws IOException {
        if (k.isAcceptable())
            ((DCCServer) k.attachment()).doAccept();

        if (k.attachment() instanceof DCCServer.UploadSession) {
            if (k.isReadable())
                ((DCCServer.UploadSession) k.attachment()).doRead();
            if (k.isWritable())
                ((DCCServer.UploadSession) k.attachment()).doWrite();
            if (!k.isValid())
                ((DCCServer.UploadSession) k.attachment()).close();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (selector.keys().size() <= 0) {
                    running = false;
                    break;
                }
            }
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (SelectionKey k : selector.selectedKeys()) {
                try {
                    handleSelectionKey(k);
                } catch (IOException err) {
                    err.printStackTrace();
                    throw new RuntimeException(err);
                }
            }
        }
    }



    void startIfNeeded() {
        synchronized (this) {
            if (running)
                return;
            running = true;
        }
        while (isAlive()) { // wait until it stops if it's running
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        start();
    }

}
