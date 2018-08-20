package io.mrarm.chatlib.irc.dcc;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DCCServerManager {

    public static final int DEFAULT_SOCKET_LIMIT = 2;

    private int socketLimit;
    private Map<UploadKey, UploadEntry> uploads = new HashMap<>();

    public DCCServerManager(int socketLimit) {
        this.socketLimit = socketLimit;
    }

    public DCCServerManager() {
        this(DEFAULT_SOCKET_LIMIT);
    }

    private UploadEntry startUpload(String user, String filename, DCCServer server) throws IOException {
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
        UploadKey key = new UploadKey(user, filename, port);
        UploadEntry ent = new UploadEntry(key, server);
        uploads.put(key, ent);
        return ent;
    }

    public UploadEntry startUpload(String user, String filename, File file) throws IOException {
        return startUpload(user, filename, new DCCServer(file, socketLimit));
    }

    public UploadEntry startUpload(String user, String filename, FileDescriptor file) throws IOException {
        return startUpload(user, filename, new DCCServer(file, socketLimit));
    }

    public void cancelUpload(UploadEntry upload) {
        uploads.remove(upload.key);
        if (upload.server != null) {
            try {
                upload.server.close();
            } catch (IOException ignored) {
            }
            upload.server = null;
        }
    }

    public static class UploadEntry {

        private UploadKey key;
        private DCCServer server;

        UploadEntry(UploadKey key, DCCServer server) {
            this.key = key;
            this.server = server;
        }

        public int getPort() {
            return server.getPort();
        }

    }

    private static class UploadKey {

        final String user;
        final String fileName;
        final int port;

        public UploadKey(String user, String fileName, int port) {
            this.user = user;
            this.fileName = fileName;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof UploadKey &&
                    this.user.equals(((UploadKey) o).user) &&
                    this.fileName.equals(((UploadKey) o).fileName) &&
                    this.port == ((UploadKey) o).port;
        }

        @Override
        public int hashCode() {
            return 31 * user.hashCode() + 7 * fileName.hashCode() + port;
        }

    }

}
