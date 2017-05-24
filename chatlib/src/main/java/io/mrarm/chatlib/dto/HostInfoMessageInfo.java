package io.mrarm.chatlib.dto;

import java.util.Date;

public class HostInfoMessageInfo extends StatusMessageInfo {

    private String serverName;
    private String version;
    private String userModes;
    private String channelModes;

    public HostInfoMessageInfo(String sender, Date date, MessageType type, String serverName, String version,
                               String userModes, String channelModes) {
        super(sender, date, type, null);
        this.serverName = serverName;
        this.version = version;
        this.userModes = userModes;
        this.channelModes = channelModes;
    }

    public String getServerName() {
        return serverName;
    }

    public String getVersion() {
        return version;
    }

    public String getUserModes() {
        return userModes;
    }

    public String getChannelModes() {
        return channelModes;
    }

}
