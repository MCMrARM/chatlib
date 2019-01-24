package io.mrarm.chatlib.irc.dcc;

import io.mrarm.chatlib.irc.ServerConnectionData;

import java.io.IOException;
import java.util.*;

public class DCCServerManager implements DCCServer.SessionListener {

    public static final int DEFAULT_SOCKET_LIMIT = 1;

    private int socketLimit;
    private final Map<UploadKey, UploadEntry> uploads = new HashMap<>();
    private final Map<UploadKey, UploadEntry> portForwardedUploads = new HashMap<>();
    private final Map<UploadKey, UploadEntry> reverseUploads = new HashMap<>();
    private final Set<Integer> reverseUploadIds = new HashSet<>();
    private final List<UploadListener> listeners = new ArrayList<>();

    public DCCServerManager(int socketLimit) {
        this.socketLimit = socketLimit;
    }

    public DCCServerManager() {
        this(DEFAULT_SOCKET_LIMIT);
    }

    protected DCCServer createServer(String filename, DCCServer.FileChannelFactory fileFactory, int socketLimit) {
        return new DCCServer(fileFactory, socketLimit);
    }

    public void addUploadListener(UploadListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeUploadListener(UploadListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public UploadEntry startUpload(ServerConnectionData connection, String user, String filename,
                                   DCCServer.FileChannelFactory factory) throws IOException {
        DCCServer server = createServer(filename, factory, socketLimit);
        server.addSessionListener(this);
        int port;
        try {
            port = server.createServerSocket();
            if (port == -1)
                throw new IOException();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                server.close();
            } catch (IOException ignored) {
            }
            throw e;
        }
        UploadKey key = new UploadKey(connection, user.toLowerCase(), filename, port);
        UploadEntry ent = new UploadEntry(key, server);
        synchronized (this) {
            uploads.put(key, ent);
            synchronized (listeners) {
                for (UploadListener listener : listeners)
                    listener.onUploadCreated(ent);
            }
        }
        return ent;
    }

    public void setUploadPortForwarded(UploadEntry upload, int port) {
        UploadKey key = new UploadKey(upload.key.connection, upload.key.user, upload.key.fileName, port);
        synchronized (this) {
            if (!uploads.containsKey(upload.key))
                return;
            if (upload.portForwardKey != null)
                portForwardedUploads.remove(upload.portForwardKey);
            upload.portForwardKey = key;
            portForwardedUploads.put(key, upload);
        }
    }

    public UploadEntry addReverseUpload(ServerConnectionData connection, String user, String filename,
                                        DCCServer.FileChannelFactory factory) {
        DCCServer server = createServer(filename, factory, socketLimit);
        server.addSessionListener(this);
        synchronized (this) {
            int id = getReverseUploadId();
            UploadKey key = new UploadKey(connection, user.toLowerCase(), filename, id);
            UploadEntry ent = new UploadEntry(key, server, id);
            reverseUploadIds.add(id);
            reverseUploads.put(key, ent);
            synchronized (listeners) {
                for (UploadListener listener : listeners)
                    listener.onUploadCreated(ent);
            }
            return ent;
        }
    }

    public synchronized UploadEntry getUploadEntry(UploadKey key) {
        UploadEntry ret = portForwardedUploads.get(key);
        if (ret != null)
            return ret;
        return uploads.get(key);
    }

    public boolean continueUpload(ServerConnectionData connection, String user, String filename, int port,
                                  long offset) {
        UploadEntry entry = getUploadEntry(new UploadKey(connection, user.toLowerCase(), filename, port));
        if (entry == null)
            return false;
        entry.server.setFileOffset(offset);
        return true;
    }

    public synchronized UploadEntry getReverseUploadEntry(UploadKey key) {
        return reverseUploads.get(key);
    }

    public void handleReverseUploadResponse(ServerConnectionData connection, String user, String filename, int uploadId,
                                            String ip, int port) {
        UploadEntry entry = getReverseUploadEntry(new UploadKey(connection, user.toLowerCase(), filename, uploadId));
        if (entry == null)
            return;
        try {
            entry.server.startReverseUpload(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelUpload(UploadEntry upload) {
        if (upload.server != null) {
            try {
                upload.server.close();
            } catch (IOException ignored) {
            }
        }
        synchronized (this) {
            uploads.remove(upload.key);
            if (upload.portForwardKey != null)
                uploads.remove(upload.portForwardKey);
            if (upload.reverseId != -1 && reverseUploads.remove(upload.key) != null) {
                reverseUploadIds.remove(upload.reverseId);
            }
            synchronized (listeners) {
                for (UploadListener listener : listeners)
                    listener.onUploadDestroyed(upload);
            }
        }
    }

    private synchronized int getReverseUploadId() {
        Random random = new Random();
        while (true) {
            int rand = random.nextInt() & Integer.MAX_VALUE;
            if (reverseUploadIds.contains(rand))
                continue;
            return rand;
        }
    }

    @Override
    public void onSessionCreated(DCCServer server, DCCServer.UploadSession session) {
        synchronized (listeners) {
            for (UploadListener listener : listeners)
                listener.onSessionCreated(server, session);
        }
    }

    @Override
    public void onSessionDestroyed(DCCServer server, DCCServer.UploadSession session) {
        synchronized (listeners) {
            for (UploadListener listener : listeners)
                listener.onSessionDestroyed(server, session);
        }
    }

    public static class UploadEntry {

        private UploadKey key;
        private UploadKey portForwardKey;
        private DCCServer server;
        private int reverseId;

        UploadEntry(UploadKey key, DCCServer server, int id) {
            this.key = key;
            this.server = server;
            this.reverseId = id;
        }
        UploadEntry(UploadKey key, DCCServer server) {
            this(key, server, -1);
        }

        public int getReverseId() {
            return reverseId;
        }

        public int getPort() {
            return server.getPort();
        }

        public int getPortForwardedPort() {
            if (portForwardKey == null)
                return -1;
            return portForwardKey.portOrId;
        }

        public String getFileName() {
            return key.fileName;
        }

        public ServerConnectionData getConnection() {
            return key.connection;
        }

        public String getUser() {
            return key.user;
        }

        public DCCServer getServer() {
            return server;
        }
    }

    private static class UploadKey {

        final ServerConnectionData connection;
        final String user;
        final String fileName;
        final int portOrId;

        public UploadKey(ServerConnectionData connection, String user, String fileName, int portOrId) {
            this.connection = connection;
            this.user = user;
            this.fileName = fileName;
            this.portOrId = portOrId;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof UploadKey &&
                    this.user.equals(((UploadKey) o).user) &&
                    this.fileName.equals(((UploadKey) o).fileName) &&
                    this.portOrId == ((UploadKey) o).portOrId &&
                    this.connection == ((UploadKey) o).connection;
        }

        @Override
        public int hashCode() {
            return 31 * connection.hashCode() + 11 * user.hashCode() + 7 * fileName.hashCode() + portOrId;
        }

    }

    public interface UploadListener extends DCCServer.SessionListener {

        void onUploadCreated(UploadEntry entry);

        void onUploadDestroyed(UploadEntry entry);

    }

}
