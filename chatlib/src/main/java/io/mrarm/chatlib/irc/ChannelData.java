package io.mrarm.chatlib.irc;

import java.util.*;
import java.util.concurrent.ExecutionException;

import io.mrarm.chatlib.ChannelInfoListener;
import io.mrarm.chatlib.MessageListener;
import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.user.UserInfo;

public class ChannelData {

    private ServerConnectionData connection;
    private String name;
    private String topic;
    private final List<MessageInfo> messages = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private Map<UUID, Member> membersMap = new HashMap<>();
    private final Object membersLock = new Object();
    private List<MessageListener> messageListeners = new ArrayList<>();
    private List<ChannelInfoListener> infoListeners = new ArrayList<>();

    public ChannelData(ServerConnectionData connection, String name) {
        this.connection = connection;
        this.name = name;
    }

    public String getName() {
        synchronized (this) {
            return name;
        }
    }

    public void setName(String name) {
        synchronized (this) {
            this.name = name;
        }
    }

    public String getTopic() {
        synchronized (this) {
            return topic;
        }
    }

    public void setTopic(String topic) {
        synchronized (this) {
            this.topic = topic;
        }
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public void addMessage(MessageInfo message) {
        synchronized (messages) {
            messages.add(message);
        }
        synchronized (messageListeners) {
            for (MessageListener listener : messageListeners)
                listener.onMessage(message);
        }
    }

    public List<Member> getMembers() {
        synchronized (membersLock) {
            return members;
        }
    }

    public List<NickWithPrefix> getMembersAsNickPrefixList() {
        synchronized (membersLock) {
            List<NickWithPrefix> list = new ArrayList<>();
            List<UUID> nickRequestList = new ArrayList<>();
            for (Member member : members)
                nickRequestList.add(member.getUserUUID());
            Map<UUID, String> nicks;
            try {
                nicks = connection.getUserInfoApi().getUsersNicks(nickRequestList, null, null).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Failed to retrieve channel nick list", e);
            }
            for (Member member : members)
                list.add(new NickWithPrefix(nicks.get(member.getUserUUID()), member.getNickPrefixes()));
            return list;
        }
    }

    private void callMemberListChanged() {
        if (infoListeners.size() > 0) {
            List<NickWithPrefix> nickWithPrefixList = getMembersAsNickPrefixList();
            synchronized (infoListeners) {
                for (ChannelInfoListener listener : infoListeners)
                    listener.onMemberListChanged(nickWithPrefixList);
            }
        }
    }

    public void addMember(Member member) {
        connection.getUserInfoApi().setUserChannelPresence(member.getUserUUID(), name, true, null, null);
        synchronized (membersLock) {
            members.add(member);
            membersMap.put(member.getUserUUID(), member);
            callMemberListChanged();
        }
    }

    public void removeMember(Member member) {
        connection.getUserInfoApi().setUserChannelPresence(member.getUserUUID(), name, false, null, null);
        synchronized (membersLock) {
            members.remove(member);
            membersMap.remove(member.getUserUUID());
            callMemberListChanged();
        }
    }

    public Member getMember(UUID userUUID) {
        synchronized (membersLock) {
            return membersMap.get(userUUID);
        }
    }

    public void setMembers(List<Member> members) {
        synchronized (membersLock) {
            for (Member member : this.members) {
                if (!members.contains(member))
                    connection.getUserInfoApi().setUserChannelPresence(member.getUserUUID(), name, false, null, null);
            }
            membersMap.clear();
            for (Member member : members) {
                if (!this.members.contains(member))
                    connection.getUserInfoApi().setUserChannelPresence(member.getUserUUID(), name, true, null, null);
                membersMap.put(member.getUserUUID(), member);
            }
            this.members = members;
        }
        callMemberListChanged();
    }

    public void subscribeMessages(MessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }

    public void unsubscribeMessages(MessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.remove(listener);
        }
    }

    public void subscribeInfo(ChannelInfoListener listener) {
        synchronized (infoListeners) {
            infoListeners.add(listener);
        }
    }

    public void unsubscribeInfo(ChannelInfoListener listener) {
        synchronized (infoListeners) {
            infoListeners.remove(listener);
        }
    }

    public static class Member {

        private UUID userUUID;
        private NickPrefixList nickPrefixes;

        public Member(UUID userUUID, NickPrefixList nickPrefixes) {
            this.userUUID = userUUID;
            this.nickPrefixes = nickPrefixes;
        }

        public UUID getUserUUID() {
            return userUUID;
        }

        public NickPrefixList getNickPrefixes() {
            return nickPrefixes;
        }

    }

}
