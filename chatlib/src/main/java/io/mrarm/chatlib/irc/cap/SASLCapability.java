package io.mrarm.chatlib.irc.cap;

import io.mrarm.chatlib.irc.CommandHandler;
import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.util.Base64Util;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SASLCapability extends Capability {

    public static final String CMD_AUTHENTICATE = "AUTHENTICATE";
    public static final int RPL_SASLSUCCESS = 903;
    public static final int ERR_SASLFAIL = 904;

    private SASLOptions[] options;
    private int currentTryOption = 0;
    private int finishLock = -1;

    public SASLCapability(SASLOptions options) {
        this.options = new SASLOptions[] { options };
    }

    public SASLCapability(SASLOptions[] options) {
        this.options = options;
    }

    @Override
    public String[] getNames() {
        return new String[] { "sasl" };
    }

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { CMD_AUTHENTICATE, RPL_SASLSUCCESS, ERR_SASLFAIL };
    }

    @Override
    public void onEnabled(ServerConnectionData connection) {
        startAuthentication(connection, 0);
        finishLock = connection.getCapabilityManager().lockNegotationFinish();
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags) throws InvalidMessageException {
        int numeric = CommandHandler.toNumeric(command);
        if (command.equals(CMD_AUTHENTICATE)) {
            if (params.size() == 1 && CommandHandler.getParamWithCheck(params, 0).equals("+"))
                continueAuthentication(connection);
        } else if (numeric == RPL_SASLSUCCESS) {
            if (finishLock != -1) {
                connection.getCapabilityManager().removeNegotationFinishLock(finishLock);
                finishLock = -1;
            }
        } else if (numeric == ERR_SASLFAIL) {
            if (currentTryOption + 1 < options.length) {
                startAuthentication(connection, currentTryOption + 1);
            } else {
                connection.getCapabilityManager().removeNegotationFinishLock(finishLock);
                finishLock = -1;
            }
        }
    }

    private SASLOptions getCurrentOptions() {
        return this.options[currentTryOption];
    }

    private void startAuthentication(ServerConnectionData connection, int optionIndex) {
        currentTryOption = optionIndex;
        SASLOptions options = getCurrentOptions();
        String method = null;
        if (options.getAuthMode() == SASLOptions.AuthMode.PLAIN)
            method = "PLAIN";
        else if (options.getAuthMode() == SASLOptions.AuthMode.EXTERNAL)
            method = "EXTERNAL";
        if (method == null)
            throw new InvalidParameterException("Invalid auth method");
        try {
            connection.getApi().sendCommand("AUTHENTICATE", false, method);
        } catch (IOException ignored) {
        }
    }

    private void continueAuthentication(ServerConnectionData connection) {
        SASLOptions options = getCurrentOptions();
        String dataStr;
        if (options.getAuthMode() == SASLOptions.AuthMode.PLAIN) {
            StringBuilder data = new StringBuilder();
            data.append(options.getUsername());
            data.append((char) 0);
            data.append(options.getUsername());
            data.append((char) 0);
            data.append(options.getPassword());
            dataStr = Base64Util.encode(data.toString().getBytes());
        } else if (options.getAuthMode() == SASLOptions.AuthMode.EXTERNAL) {
            dataStr = "+";
        } else {
            throw new InvalidParameterException("Invalid auth method");
        }
        try {
            connection.getApi().sendCommand("AUTHENTICATE", false, dataStr);
        } catch (IOException ignored) {
        }
    }

}
