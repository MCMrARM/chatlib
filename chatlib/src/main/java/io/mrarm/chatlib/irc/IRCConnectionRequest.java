package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.List;

public class IRCConnectionRequest {

    private String serverIP;
    private int serverPort;
    private String user;
    private int userMode;
    private String realname;
    private List<String> nickList;

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getUser() {
        return user;
    }

    public IRCConnectionRequest setUser(String user) {
        this.user = user;
        return this;
    }

    public int getUserMode() {
        return userMode;
    }

    public IRCConnectionRequest setUserMode(int userMode) {
        this.userMode = userMode;
        return this;
    }

    public String getRealName() {
        return realname;
    }

    public IRCConnectionRequest setRealName(String realname) {
        this.realname = realname;
        return this;
    }

    public IRCConnectionRequest setServerAddress(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        return this;
    }

    public List<String> getNickList() {
        return nickList;
    }

    public IRCConnectionRequest setNickList(List<String> nickList) {
        this.nickList = nickList;
        return this;
    }

    public IRCConnectionRequest addNick(String nick) {
        if (nickList == null)
            nickList = new ArrayList<>();
        nickList.add(nick);
        return this;
    }

}
