package io.mrarm.chatlib;

import io.mrarm.chatlib.dto.StatusMessageInfo;

public interface StatusMessageListener {

    void onStatusMessage(StatusMessageInfo message);

}
