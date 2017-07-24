package io.mrarm.chatlib.dto;

import java.util.List;

public class ChannelList {

    private List<Entry> entries;

    public ChannelList(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {

        private String channel;
        private int memberCount;
        private String topic;

        public Entry(String channel, int memberCount, String topic) {
            this.channel = channel;
            this.memberCount = memberCount;
            this.topic = topic;
        }

        public String getChannel() {
            return channel;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public String getTopic() {
            return topic;
        }

    }

}
