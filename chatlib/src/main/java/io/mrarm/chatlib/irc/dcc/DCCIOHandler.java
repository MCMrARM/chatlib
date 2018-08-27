package io.mrarm.chatlib.irc.dcc;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;

public class DCCIOHandler implements Runnable {

    private static final DCCIOHandler instanceSingleton = new DCCIOHandler();

    public static DCCIOHandler getInstance() {
        return instanceSingleton;
    }


    private Thread thread;
    private Selector selector;
    private final Object selectorLock = new Object();
    private boolean running = false;

    public DCCIOHandler() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SelectionKey register(AbstractSelectableChannel channel, int ops, SelectHandler callback)
            throws ClosedChannelException {
        SelectionKey ret;
        synchronized (selectorLock) {
            selector.wakeup();
            ret = channel.register(selector, ops, callback);
        }
        startIfNeeded();
        return ret;
    }

    public void unregister(SelectionKey key) {
        synchronized (selectorLock) {
            selector.wakeup();
            key.cancel();
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (selectorLock) {
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
                    ((SelectHandler) k.attachment()).onSelect(k);
                } catch (CancelledKeyException ignored) {
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }
    }



    void startIfNeeded() {
        synchronized (selectorLock) {
            if (running)
                return;
            running = true;
        }
        thread = new Thread(this);
        thread.start();
    }


    public interface SelectHandler {

        void onSelect(SelectionKey key) throws IOException;

    }

}
