package com.github.kafka_tools.local_communications;

import java.nio.MappedByteBuffer;

/**
 * Author: Evgeny Zhoga
 * Date: 14.10.14
 */
public interface ClientInfo {
    public abstract void write(MemWriter writer);
}
