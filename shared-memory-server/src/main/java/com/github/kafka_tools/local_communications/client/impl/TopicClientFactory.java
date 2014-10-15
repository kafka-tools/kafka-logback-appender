package com.github.kafka_tools.local_communications.client.impl;

import com.github.kafka_tools.local_communications.CommunicationInfo;

/**
 * Author: Evgeny Zhoga
 * Date: 15.10.14
 */
public class TopicClientFactory extends CommunicationInfo.Factory<TopicClient> {
    @Override
    public CommunicationInfo<TopicClient> build(String streamName, int bufferSize) {
        return new TopicClientCommunicationInfo(streamName, bufferSize);
    }
}
