package io.mrarm.chatlib.dto;

import java.util.ArrayList;
import java.util.List;

public class WhoisInfo {

    private String nick;
    private String user;
    private String host;
    private String realName;
    private String server;
    private String serverInfo;
    private boolean operator;
    private int idle;
    private List<ChannelWithNickPrefixes> channels;
    private String account;
    private boolean secureConnection;
    private String awayMessage;

    public String getNick() {
        return nick;
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getRealName() {
        return realName;
    }

    public String getServer() {
        return server;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public boolean isOperator() {
        return operator;
    }

    public int getIdleSeconds() {
        return idle;
    }

    public List<ChannelWithNickPrefixes> getChannels() {
        return channels;
    }

    public String getLoggedInAsAccount() {
        return account;
    }

    public boolean isConnectionSecure() {
        return secureConnection;
    }

    public String getAwayMessage() {
        return awayMessage;
    }

    public static class ChannelWithNickPrefixes {

        private String channel;
        private NickPrefixList prefixes;

        public ChannelWithNickPrefixes(String channel, NickPrefixList prefixes) {
            this.channel = channel;
            this.prefixes = prefixes;
        }

        public String getChannel() {
            return channel;
        }

        public NickPrefixList getPrefixes() {
            return prefixes;
        }

    }

    public static class Builder {

        private WhoisInfo object = new WhoisInfo();

        public void setUserInfo(String nick, String user, String host, String realName) {
            object.nick = nick;
            object.user = user;
            object.host = host;
            object.realName = realName;
        }

        public void setServerInfo(String server, String serverInfo) {
            object.server = server;
            object.serverInfo = serverInfo;
        }

        public void setOperator(boolean operator) {
            object.operator = operator;
        }

        public void setIdle(int seconds) {
            object.idle = seconds;
        }

        public void addChannel(ChannelWithNickPrefixes channel) {
            if (object.channels == null)
                object.channels = new ArrayList<>();
            object.channels.add(channel);
        }

        public void setAccount(String account) {
            object.account = account;
        }

        public void setSecure(boolean secureConnection) {
            object.secureConnection = secureConnection;
        }

        public void setAway(String message) {
            object.awayMessage = message;
        }

        public WhoisInfo build() {
            WhoisInfo ret = object;
            object = null;
            return ret;
        }

    }

}
