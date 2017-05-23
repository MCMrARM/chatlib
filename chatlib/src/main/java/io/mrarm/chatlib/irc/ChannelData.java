package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mrarm.chatlib.MessageListener;
import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.user.UserInfo;

public class ChannelData {

    private ServerConnectionData connection;
    private String name;
    private String title;
    private List<MessageInfo> messages = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private Map<UserInfo, Member> membersMap = new HashMap<>();
    private List<MessageListener> messageListeners = new ArrayList<>();

    public ChannelData(ServerConnectionData connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(MessageInfo message) {
        messages.add(message);
        for (MessageListener listener : messageListeners)
            listener.onMessage(message);
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<NickWithPrefix> getMembersAsNickPrefixList() {
        List<NickWithPrefix> list = new ArrayList<>();
        for (Member member : members)
            list.add(new NickWithPrefix(member.getUserInfo().getCurrentNick(), member.getNickPrefixes()));
        return list;
    }

    public void addMember(Member member) {
        connection.getUserInfoApi().setUserChannelPresence(member.getUserInfo().getUUID(), name, true, null, null);
        members.add(member);
        membersMap.put(member.getUserInfo(), member);
    }

    public void removeMember(Member member) {
        connection.getUserInfoApi().setUserChannelPresence(member.getUserInfo().getUUID(), name, false, null, null);
        members.remove(member);
        membersMap.remove(member.getUserInfo());
    }

    public Member getMember(UserInfo user) {
        return membersMap.get(user);
    }

    public void setMembers(List<Member> members) {
        for (Member member : this.members) {
            if (!members.contains(member))
                connection.getUserInfoApi().setUserChannelPresence(member.getUserInfo().getUUID(), name, false, null, null);
        }
        membersMap.clear();
        for (Member member : members) {
            if (!this.members.contains(member))
                connection.getUserInfoApi().setUserChannelPresence(member.getUserInfo().getUUID(), name, true, null, null);
            membersMap.put(member.getUserInfo(), member);
        }
        this.members = members;
    }

    public void subscribeMessages(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void unsubscribeMessages(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public static class Member {

        private UserInfo userInfo;
        private NickPrefixList nickPrefixes;

        public Member(UserInfo userInfo, NickPrefixList nickPrefixes) {
            this.userInfo = userInfo;
            this.nickPrefixes = nickPrefixes;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public NickPrefixList getNickPrefixes() {
            return nickPrefixes;
        }

    }

}
