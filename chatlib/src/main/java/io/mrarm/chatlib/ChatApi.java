package io.mrarm.chatlib;

import java.util.List;

import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;

public interface ChatApi {

    void getJoinedChannelList(ResponseCallback<List<String>> callback,
                              ResponseErrorCallback errorCallback);

    void getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                        ResponseErrorCallback errorCallback);

    void getMessages(String channelName, int count, MessageList after,
                     ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback);

}
