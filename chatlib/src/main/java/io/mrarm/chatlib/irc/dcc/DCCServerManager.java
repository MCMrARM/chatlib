package io.mrarm.chatlib.irc.dcc;

import io.mrarm.chatlib.irc.ServerConnectionData;

import java.io.IOException;
import java.util.*;

public class DCCServerManager {

    public static final int DEFAULT_SOCKET_LIMIT = 1;

    private int socketLimit;
    private Map<UploadKey, UploadEntry> uploads = new HashMap<>();
    private Map<UploadKey, UploadEntry> reverseUploads = new HashMap<>();
    private Set<Integer> reverseUploadIds = new HashSet<>();

    public DCCServerManager(int socketLimit) {
        this.socketLimit = socketLimit;
    }

    public DCCServerManager() {
        this(DEFAULT_SOCKET_LIMIT);
    }

    protected DCCServer createServer(DCCServer.FileChannelFactory fileFactory, int socketLimit) {
        return new DCCServer(fileFactory, socketLimit);
    }

    public UploadEntry startUpload(ServerConnectionData connection, String user, String filename,
                                   DCCServer.FileChannelFactory factory) throws IOException {
        DCCServer server = createServer(factory, socketLimit);
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
        }
        return ent;
    }

    public UploadEntry addReverseUpload(ServerConnectionData connection, String user, String filename,
                                        DCCServer.FileChannelFactory factory) {
        DCCServer server = createServer(factory, socketLimit);
        synchronized (this) {
            int id = getReverseUploadId();
            UploadKey key = new UploadKey(connection, user.toLowerCase(), filename, id);
            UploadEntry ent = new UploadEntry(key, server, id);
            reverseUploadIds.add(id);
            reverseUploads.put(key, ent);
            return ent;
        }
    }

    public synchronized UploadEntry getUploadEntry(UploadKey key) {
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
        synchronized (this) {
            uploads.remove(upload.key);
            if (upload.reverseId != -1 && reverseUploads.remove(upload.key) != null) {
                reverseUploadIds.remove(upload.reverseId);
            }
        }
        if (upload.server != null) {
            try {
                upload.server.close();
            } catch (IOException ignored) {
            }
            upload.server = null;
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

    public static class UploadEntry {

        private UploadKey key;
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

}
