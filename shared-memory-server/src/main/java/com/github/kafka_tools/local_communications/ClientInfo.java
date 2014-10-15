package com.github.kafka_tools.local_communications;

import java.nio.MappedByteBuffer;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 14.10.14
 */
public interface ClientInfo {
    public abstract void write(MemWriter writer);
}
