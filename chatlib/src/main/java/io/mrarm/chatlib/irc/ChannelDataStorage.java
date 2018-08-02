package io.mrarm.chatlib.irc;

import java.util.Date;
import java.util.concurrent.Future;

public interface ChannelDataStorage {

    class StoredData {

        private String topic;
        private String topicSetBy;
        private Date topicSetOn;

        public StoredData(String topic, String topicSetBy, Date topicSetOn) {
            this.topic = topic;
            this.topicSetBy = topicSetBy;
            this.topicSetOn = topicSetOn;
        }

        public String getTopic() {
            return topic;
        }

        public String getTopicSetBy() {
            return topicSetBy;
        }

        public Date getTopicSetOn() {
            return topicSetOn;
        }

    }

    Future<StoredData> getOrCreateChannelData(String channel);

    Future<Void> updateTopic(String channel, String topic, String setBy, Date setOn);

}
