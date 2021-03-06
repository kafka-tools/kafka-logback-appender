package com.github.kafka_tools.local_communications;

import com.github.kafka_tools.local_communications.util.MemReader;
import com.github.kafka_tools.local_communications.util.Util;

/**
 * Author: Evgeny Zhoga
 * Date: 13.10.14
 */
public abstract class CommunicationInfo<T extends ClientInfo> {
    private final String streamName;
    private final int bufferSize;

    public CommunicationInfo(String streamName, int bufferSize) {
        this.streamName = streamName;
        this.bufferSize = bufferSize;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getWatchdogStreamName() {
        return Util.getWatchdogThreadName(streamName);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public abstract T read(MemReader in);

    public static abstract class Factory<T extends ClientInfo> {
        public abstract CommunicationInfo<T> build(String streamName, int bufferSize);
    }
}
