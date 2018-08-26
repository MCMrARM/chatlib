package io.mrarm.chatlib.irc.dcc;

import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;

public abstract class DCCClientManager {

    public DCCClientManager() {
        //
    }

    public abstract void onFileOffered(ServerConnectionData connection, MessagePrefix sender, String filename,
                                       String address, int port, long fileSize);

    public abstract void onFileOfferedUsingReverse(ServerConnectionData connection, MessagePrefix sender,
                                                   String filename, long fileSize, int uploadId);

}
