package io.mrarm.chatlib.irc;

import java.util.*;

public class ErrorCommandHandler implements CommandHandler {

    private final Map<Integer, Queue<ErrorCallback>> callbacks = new HashMap<>();

    public void addErrorCallback(int id, ErrorCallback cb) {
        synchronized (callbacks) {
            if (!callbacks.containsKey(id))
                callbacks.put(id, new ArrayDeque<>());
            callbacks.get(id).add(cb);
        }
    }

    public void addErrorCallback(int[] ids, ErrorCallback cb) {
        synchronized (callbacks) {
            for (int id : ids)
                addErrorCallback(id, cb);
        }
    }

    public void cancelErrorCallback(int id, ErrorCallback cb) {
        synchronized (callbacks) {
            Queue<ErrorCallback> l = callbacks.get(id);
            if (l == null)
                return;
            l.remove(cb);
        }
    }

    public void cancelErrorCallback(int[] ids, ErrorCallback cb) {
        synchronized (callbacks) {
            for (int id : ids)
                cancelErrorCallback(id, cb);
        }
    }

    public boolean canHandle(String command) {
        if (command.length() != 3)
            return false;
        int cmdId;
        try {
            cmdId = Integer.parseInt(command);
        } catch (NumberFormatException ex) {
            return false;
        }
        synchronized (callbacks) {
            return callbacks.containsKey(cmdId);
        }
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        synchronized (callbacks) {
            Queue<ErrorCallback> l = callbacks.get(numeric);
            if (l == null || l.size() == 0)
                throw new InvalidMessageException("Didn't find any callbacks");
            boolean f = true;
            for (ErrorCallback cb : l) {
                if (cb.onError(numeric, params)) {
                    if (f) {
                        l.remove();
                        return;
                    }
                    l.remove(cb);
                    return;
                }
                f = false;
            }
            throw new InvalidMessageException("No callback was found to process this message");
        }
    }

    @Override
    public Object[] getHandledCommands() {
        throw new UnsupportedOperationException();
    }

    public interface ErrorCallback {

        boolean onError(int commandId, List<String> params);

    }

}
