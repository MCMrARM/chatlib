package io.mrarm.chatlib.dto;

import java.util.Date;
import java.util.List;

public class ChannelModeMessageInfo extends MessageInfo {

    public enum EntryType {
        LIST, VALUE_EXACT_UNSET, VALUE, FLAG, NICK_FLAG
    }

    private List<Entry> entries;

    public ChannelModeMessageInfo(MessageSenderInfo sender, Date date, List<Entry> entries) {
        super(sender, date, null, MessageType.MODE);
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {

        private EntryType type;
        private char mode;
        private String param;
        private boolean removed;

        public Entry(EntryType type, char mode, String param, boolean removed) {
            this.type = type;
            this.mode = mode;
            this.param = param;
            this.removed = removed;
        }

        public EntryType getType() {
            return type;
        }

        public char getMode() {
            return mode;
        }

        public String getParam() {
            return param;
        }

        public boolean isRemoved() {
            return removed;
        }

    }

    public static class Builder extends MessageInfo.Builder {

        public Builder(MessageSenderInfo sender, List<Entry> entries) {
            messageInfo = new ChannelModeMessageInfo(sender, new Date(), entries);
        }

        public ChannelModeMessageInfo build() {
            ChannelModeMessageInfo ret = (ChannelModeMessageInfo) messageInfo;
            messageInfo = null;
            return ret;
        }

    }

}
