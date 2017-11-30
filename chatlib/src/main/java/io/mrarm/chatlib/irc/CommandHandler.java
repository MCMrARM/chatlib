package io.mrarm.chatlib.irc;

import java.util.List;
import java.util.Map;

public interface CommandHandler {

    static int toNumeric(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static String getParamWithCheck(List<String> params, int index) throws InvalidMessageException {
        if (index < 0 || index >= params.size())
            throw new InvalidMessageException("Missing parameter");
        return params.get(index);
    }

    static String getParamOrDefault(List<String> params, int index, String def) {
        if (index < 0 || index >= params.size())
            return def;
        return params.get(index);
    }

    static String getParamOrNull(List<String> params, int index) {
        return getParamOrDefault(params, index, null);
    }

    Object[] getHandledCommands();

    void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                Map<String, String> tags)
            throws InvalidMessageException;

}
