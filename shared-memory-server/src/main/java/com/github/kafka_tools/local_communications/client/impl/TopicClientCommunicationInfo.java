package com.github.kafka_tools.local_communications.client.impl;

import com.github.kafka_tools.local_communications.CommunicationInfo;
import com.github.kafka_tools.local_communications.util.MemReader;

/**
 * Author: Evgeny Zhoga
 * Date: 15.10.14
 */
public class TopicClientCommunicationInfo extends CommunicationInfo<TopicClient> {
    TopicClientCommunicationInfo(String streamName, int bufferSize) {
        super(streamName, bufferSize);
    }

    @Override
    public TopicClient read(MemReader in) {
        return new TopicClient(in.readString());
    }
}
