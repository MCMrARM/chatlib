package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ServerTimeCapability extends Capability {

    private SimpleDateFormat parser;

    public ServerTimeCapability() {
        parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String[] getNames() {
        return new String[] { "server-time", "znc.in/server-time-iso" };
    }

    @Override
    public String[] getHandledCommands() {
        return new String[0];
    }

    @Override
    public void processMessage(MessageInfo.Builder message, Map<String, String> tags) {
        if (tags.containsKey("time")) {
            try {
                message.setDate(parser.parse(tags.get("time")));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        // stub
    }

}
