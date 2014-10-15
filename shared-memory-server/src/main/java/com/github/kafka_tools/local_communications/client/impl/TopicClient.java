package com.github.kafka_tools.local_communications.client.impl;

import com.github.kafka_tools.local_communications.ClientInfo;
import com.github.kafka_tools.local_communications.MemWriter;

/**
 * Author: Evgeny Zhoga
 * Date: 15.10.14
 */
public class TopicClient implements ClientInfo {
    public final String topic;

    public TopicClient(String topic) {
        this.topic = topic;
    }

    @Override
    public void write(MemWriter writer) {
        writer.write(topic);
    }
}
