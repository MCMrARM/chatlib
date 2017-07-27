package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.ResponseCallback;
import io.mrarm.chatlib.ResponseErrorCallback;
import io.mrarm.chatlib.dto.ChannelList;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.NumericCommandHandler;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.util.SettableFuture;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

public class ListCommandHandler extends NumericCommandHandler {

    public static final int RPL_LISTSTART = 321;
    public static final int RPL_LIST = 322;
    public static final int RPL_LISTEND = 323;

    private Request currentRequest;
    private final ArrayDeque<Request> requests = new ArrayDeque<>();

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_LISTSTART, RPL_LIST, RPL_LISTEND };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        Request request;
        synchronized (requests) {
            request = currentRequest;
        }
        if (request == null)
            throw new InvalidMessageException("Channel list entry without a request context");
        if (command == RPL_LISTSTART) {
            request.entries = new ArrayList<>();
        } else if (command == RPL_LISTEND) {
            ChannelList resp = new ChannelList(currentRequest.entries);
            request.retVal.set(resp);
            if (request.callback != null)
                request.callback.onResponse(resp);
            handleNextRequest(connection);
        } else if (command == RPL_LIST) {
            if (request.entries == null)
                throw new InvalidMessageException("Channel list entry without a list start message");
            ChannelList.Entry entry = new ChannelList.Entry(params.get(1), Integer.parseInt(params.get(2)), params.get(3));
            request.entries.add(entry);
            if (request.entryCallback != null)
                request.entryCallback.onResponse(entry);
        }
    }


    public Future<ChannelList> addRequest(ServerConnectionData connection, ResponseCallback<ChannelList> callback,
                                          ResponseCallback<ChannelList.Entry> entryCallback,
                                          ResponseErrorCallback errorCallback) {
        Request request = new Request();
        request.retVal = new SettableFuture<>();
        request.callback = callback;
        request.entryCallback = entryCallback;
        request.errorCallback = errorCallback;
        synchronized (requests) {
            requests.add(request);
            if (currentRequest == null)
                handleNextRequest(connection);
        }
        return request.retVal;
    }

    private void sendRequest(ServerConnectionData connection, Request request) throws IOException {
        synchronized (this) {
            connection.getApi().sendCommand("LIST", false, null, null, null);
        }
    }

    private void handleNextRequest(ServerConnectionData connection) {
        synchronized (requests) {
            if (!requests.isEmpty()) {
                currentRequest = requests.remove();
                try {
                    sendRequest(connection, currentRequest);
                } catch (Exception e) {
                    if (currentRequest.errorCallback != null)
                        currentRequest.errorCallback.onError(e);
                    currentRequest.retVal.setExecutionException(e);
                }
            } else {
                currentRequest = null;
            }
        }
    }

    private static class Request {
        private SettableFuture<ChannelList> retVal;
        private ResponseCallback<ChannelList> callback;
        private ResponseCallback<ChannelList.Entry> entryCallback;
        private ResponseErrorCallback errorCallback;
        private List<ChannelList.Entry> entries;
    }

}