package io.mrarm.chatlib.irc;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IRCConnectionRequest {

    private String serverIP;
    private int serverPort;
    private boolean serverSSL = false;
    private SocketFactory sslSocketFactory;
    private HostnameVerifier sslHostnameVerifier;
    private Charset charset = Charset.forName("UTF-8");
    private String serverPass;
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

    public boolean isUsingSSL() {
        return serverSSL;
    }

    public SocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    public HostnameVerifier getSSLHostnameVerifier() {
        return sslHostnameVerifier;
    }

    public Charset getCharset() {
        return charset;
    }

    public IRCConnectionRequest setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public String getServerPass() {
        return serverPass;
    }

    public void setServerPass(String serverPass) {
        this.serverPass = serverPass;
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

    public IRCConnectionRequest enableSSL(SocketFactory factory, HostnameVerifier verifier) {
        this.sslSocketFactory = factory;
        this.sslHostnameVerifier = verifier;
        this.serverSSL = true;
        return this;
    }

    public IRCConnectionRequest disableSSL() {
        this.serverSSL = false;
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
