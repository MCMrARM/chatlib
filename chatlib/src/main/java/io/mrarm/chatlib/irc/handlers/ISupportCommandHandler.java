package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.ModeList;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;

public class ISupportCommandHandler implements CommandHandler {

    public static final int RPL_ISUPPORT = 5;

    public static final String PARAM_PREFIX_LIST = "PREFIX";
    public static final String PARAM_CHANTYPES = "CHANTYPES";
    public static final String PARAM_CHANMODES = "CHANMODES";

    @Override
    public Object[] getHandledCommands() {
        return new Object[] { RPL_ISUPPORT };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params,
                       Map<String, String> tags)
            throws InvalidMessageException {
        int count = params.size() - 1;
        for (int i = 0; i < count; i++) {
            String param = params.get(i);
            String value = null;
            boolean remove = false;
            if (param.startsWith("-")) {
                remove = true;
                param = param.substring(1);
            }
            int iof = param.indexOf('=');
            if (iof != -1) {
                value = param.substring(iof + 1);
                param = param.substring(0, iof);
            }
            handle(connection.getSupportList(), param, value, remove);
        }
    }

    private void handle(ServerSupportList supportList, String param, String value, boolean remove) {
        if (param.equals(PARAM_PREFIX_LIST)) {
            if (value.startsWith("(")) {
                int iof = value.indexOf(')');
                if (iof != -1) {
                    supportList.setSupportedNickPrefixModes(new ModeList(value.substring(1, iof)));
                    value = value.substring(iof + 1);
                }
            }
            supportList.setSupportedNickPrefixes(new NickPrefixList(value));
        } else if (param.equals(PARAM_CHANTYPES)) {
            supportList.setSupportedChannelTypes(new ModeList(value));
        } else if (param.equals(PARAM_CHANMODES)) {
            String[] modes = value.split(",", -1);
            supportList.setSupportedChannelModes(new ModeList(modes[0]), new ModeList(modes[1]), new ModeList(modes[2]),
                    new ModeList(modes[3]));
        }
    }

}
