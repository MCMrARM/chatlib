package io.mrarm.chatlib;

import java.util.List;
import java.util.concurrent.Future;

import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;

public interface ChatApi {

    Future<List<String>> getJoinedChannelList(ResponseCallback<List<String>> callback,
                                              ResponseErrorCallback errorCallback);

    Future<ChannelInfo> getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                                       ResponseErrorCallback errorCallback);

    Future<MessageList> getMessages(String channelName, int count, MessageList after,
                                    ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback);

}
