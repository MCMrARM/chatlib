package io.mrarm.chatlib.irc;

import java.util.*;
import java.util.concurrent.ExecutionException;

import io.mrarm.chatlib.ChannelInfoListener;
import io.mrarm.chatlib.dto.ModeList;
import io.mrarm.chatlib.dto.NickWithPrefix;
import io.mrarm.chatlib.dto.NickPrefixList;

public class ChannelData {

    private ServerConnectionData connection;
    private String name;
    private String topic;
    private List<Member> members = new ArrayList<>();
    private Map<UUID, Member> membersMap = new HashMap<>();
    private final Object membersLock = new Object();
    private final List<ChannelInfoListener> infoListeners = new ArrayList<>();
    private Map<Character, Set<String>> modesList;
    private Map<Character, String> modesValueExactUnset;
    private Map<Character, String> modesValue;
    private ModeList modesFlag;

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

    public void callMemberListChanged() {
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

    public void setMemberModeList(Member member, ModeList modeList) {
        synchronized (membersLock) {
            member.modeList = modeList;
        }
    }

    public void setMemberNickPrefixes(Member member, NickPrefixList prefixes) {
        synchronized (membersLock) {
            member.nickPrefixes = prefixes;
            callMemberListChanged();
        }
    }

    public ModeList getFlagModes() {
        synchronized (this) {
            return modesFlag;
        }
    }

    public boolean getFlagMode(char flag) {
        synchronized (this) {
            if (modesFlag == null)
                return false;
            return modesFlag.contains(flag);
        }
    }

    public void setFlagMode(char flag, boolean set) {
        synchronized (this) {
            if (modesFlag == null) {
                if (set)
                    modesFlag = new ModeList(String.valueOf(flag));
                return;
            }
            int iof = modesFlag.find(flag);
            if ((iof != -1) == set)
                return;
            String str = modesFlag.toString();
            if (set)
                modesFlag = new ModeList(str + flag);
            else
                modesFlag = new ModeList(str.substring(0, iof) + str.substring(iof + 1));
        }
    }

    public Map<Character, Set<String>> getListModes() {
        synchronized (this) {
            return modesList;
        }
    }

    public void addListMode(char flag, String val) {
        synchronized (this) {
            if (modesList == null)
                modesList = new HashMap<>();
            if (!modesList.containsKey(flag))
                modesList.put(flag, new HashSet<>());
            modesList.get(flag).add(val);
        }
    }

    public void removeListMode(char flag, String val) {
        synchronized (this) {
            if (modesList == null)
                return;
            Set<String> s = modesList.get(flag);
            if (s != null) {
                s.remove(val);
                if (s.size() == 0)
                    modesList.remove(flag);
            }
        }
    }

    public void setValueExactUnsetMode(char flag, String val) {
        synchronized (this) {
            if (modesValueExactUnset == null)
                modesValueExactUnset = new HashMap<>();
            modesValueExactUnset.put(flag, val);
        }
    }

    public void removeValueExactUnsetMode(char flag) {
        synchronized (this) {
            if (modesValueExactUnset == null)
                return;
            modesValueExactUnset.remove(flag);
        }
    }

    public void setValueMode(char flag, String val) {
        synchronized (this) {
            if (modesValue == null)
                modesValue = new HashMap<>();
            modesValue.put(flag, val);
        }
    }

    public void removeValueMode(char flag) {
        synchronized (this) {
            if (modesValue == null)
                return;
            modesValue.remove(flag);
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
        private ModeList modeList;
        private NickPrefixList nickPrefixes;

        public Member(UUID userUUID, ModeList modeList, NickPrefixList nickPrefixes) {
            this.userUUID = userUUID;
            this.modeList = modeList;
            this.nickPrefixes = nickPrefixes;
        }

        public UUID getUserUUID() {
            return userUUID;
        }

        public ModeList getModeList() {
            return modeList;
        }

        public NickPrefixList getNickPrefixes() {
            return nickPrefixes;
        }

    }

}
