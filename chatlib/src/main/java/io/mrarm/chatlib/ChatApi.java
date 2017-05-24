package io.mrarm.chatlib;

import java.util.List;
import java.util.concurrent.Future;

import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.MessageList;
import io.mrarm.chatlib.dto.StatusMessageList;
import io.mrarm.chatlib.user.UserInfoApi;

public interface ChatApi {

    UserInfoApi getUserInfoApi();

    Future<List<String>> getJoinedChannelList(ResponseCallback<List<String>> callback,
                                              ResponseErrorCallback errorCallback);

    Future<ChannelInfo> getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                                       ResponseErrorCallback errorCallback);

    Future<MessageList> getMessages(String channelName, int count, MessageList after,
                                    ResponseCallback<MessageList> callback, ResponseErrorCallback errorCallback);

    Future<StatusMessageList> getStatusMessages(StatusMessageList after, ResponseCallback<StatusMessageList> callback,
                                                ResponseErrorCallback errorCallback);

    Future<Void> subscribeChannelMessages(String channelName, MessageListener listener, ResponseCallback<Void> callback,
                                          ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeChannelMessages(String channelName, MessageListener listener,
                                            ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

}
