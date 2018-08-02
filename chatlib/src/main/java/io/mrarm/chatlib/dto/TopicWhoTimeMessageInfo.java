package io.mrarm.chatlib.dto;

import java.util.Date;

public class TopicWhoTimeMessageInfo extends MessageInfo {

    private MessageSenderInfo who;
    private Date when;

    public TopicWhoTimeMessageInfo(MessageSenderInfo sender, Date date, MessageSenderInfo who, Date when) {
        super(sender, date, null, MessageType.TOPIC_WHOTIME);
        this.who = who;
        this.when = when;
    }

    public MessageSenderInfo getSetBy() {
        return who;
    }

    public Date getSetOnDate() {
        return when;
    }

    public static class Builder extends MessageInfo.Builder {

        public Builder(MessageSenderInfo sender, MessageSenderInfo who, Date when) {
            messageInfo = new TopicWhoTimeMessageInfo(sender, new Date(), who, when);
        }

        public TopicWhoTimeMessageInfo build() {
            TopicWhoTimeMessageInfo ret = (TopicWhoTimeMessageInfo) messageInfo;
            messageInfo = null;
            return ret;
        }

    }

}
