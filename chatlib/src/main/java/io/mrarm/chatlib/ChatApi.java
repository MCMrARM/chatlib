package io.mrarm.chatlib;

import java.util.List;
import java.util.concurrent.Future;

import io.mrarm.chatlib.dto.ChannelInfo;
import io.mrarm.chatlib.dto.ChannelList;
import io.mrarm.chatlib.dto.StatusMessageList;
import io.mrarm.chatlib.dto.WhoisInfo;
import io.mrarm.chatlib.message.MessageStorageApi;
import io.mrarm.chatlib.user.UserInfoApi;

public interface ChatApi {

    UserInfoApi getUserInfoApi();

    MessageStorageApi getMessageStorageApi();

    Future<Void> quit(String message, ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

    Future<WhoisInfo> sendWhois(String nick, ResponseCallback<WhoisInfo> callback, ResponseErrorCallback errorCallback);

    Future<Void> joinChannels(List<String> channels, ResponseCallback<Void> callback,
                              ResponseErrorCallback errorCallback);

    Future<Void> leaveChannel(String channel, String reason, ResponseCallback<Void> callback,
                              ResponseErrorCallback errorCallback);

    Future<Void> sendMessage(String channel, String message, ResponseCallback<Void> callback,
                             ResponseErrorCallback errorCallback);

    Future<Void> sendNotice(String channel, String message, ResponseCallback<Void> callback,
                            ResponseErrorCallback errorCallback);

    Future<ChannelList> listChannels(ResponseCallback<ChannelList> callback,
                                     ResponseCallback<ChannelList.Entry> entryCallback,
                                     ResponseErrorCallback errorCallback);

    Future<List<String>> getJoinedChannelList(ResponseCallback<List<String>> callback,
                                              ResponseErrorCallback errorCallback);

    Future<ChannelInfo> getChannelInfo(String channelName, ResponseCallback<ChannelInfo> callback,
                                       ResponseErrorCallback errorCallback);

    Future<StatusMessageList> getStatusMessages(int count, StatusMessageList after,
                                                ResponseCallback<StatusMessageList> callback,
                                                ResponseErrorCallback errorCallback);

    Future<Void> subscribeChannelList(ChannelListListener listener, ResponseCallback<Void> callback,
                                      ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeChannelList(ChannelListListener listener, ResponseCallback<Void> callback,
                                        ResponseErrorCallback errorCallback);

    Future<Void> subscribeChannelInfo(String channelName, ChannelInfoListener listener, ResponseCallback<Void> callback,
                                      ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeChannelInfo(String channelName, ChannelInfoListener listener,
                                        ResponseCallback<Void> callback, ResponseErrorCallback errorCallback);

    Future<Void> subscribeStatusMessages(StatusMessageListener listener, ResponseCallback<Void> callback,
                                         ResponseErrorCallback errorCallback);

    Future<Void> unsubscribeStatusMessages(StatusMessageListener listener, ResponseCallback<Void> callback,
                                           ResponseErrorCallback errorCallback);

}
