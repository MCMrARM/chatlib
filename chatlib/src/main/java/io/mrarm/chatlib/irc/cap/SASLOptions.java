package io.mrarm.chatlib.irc.cap;

public class SASLOptions {

    public enum AuthMode {
        PLAIN, EXTERNAL
    }

    private AuthMode authMode;
    private String username;
    private String password;

    public AuthMode getAuthMode() {
        return authMode;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setAuthMode(AuthMode authMode) {
        this.authMode = authMode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static SASLOptions createPlainAuth(String username, String password) {
        SASLOptions ret = new SASLOptions();
        ret.authMode = AuthMode.PLAIN;
        ret.username = username;
        ret.password = password;
        return ret;
    }

    public static SASLOptions createExternal() {
        SASLOptions ret = new SASLOptions();
        ret.authMode = AuthMode.EXTERNAL;
        return ret;
    }

}
