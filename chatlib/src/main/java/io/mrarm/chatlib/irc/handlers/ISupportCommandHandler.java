package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.irc.*;

import java.util.List;
import java.util.Map;

public class ISupportCommandHandler extends NumericCommandHandler {

    public static final int RPL_ISUPPORT = 5;

    public static final String PARAM_PREFIX_LIST = "PREFIX";

    @Override
    public int[] getNumericHandledCommands() {
        return new int[] { RPL_ISUPPORT };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, int command, List<String> params,
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
                    supportList.setSupportedNickPrefixModes(value.substring(1, iof).toCharArray());
                    value = value.substring(iof + 1);
                }
            }
            supportList.setSupportedNickPrefixes(new NickPrefixList(value));
        }
    }

}
