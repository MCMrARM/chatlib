package io.mrarm.chatlib.irc;

import java.util.concurrent.Future;

public interface ChannelDataStorage {

    class StoredData {

        private String topic;

        public StoredData(String topic) {
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

    }

    Future<StoredData> getOrCreateChannelData(String channel);

    Future<Void> updateTopic(String channel, String topic);

}
