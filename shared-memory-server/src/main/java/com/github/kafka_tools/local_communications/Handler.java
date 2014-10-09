package com.github.kafka_tools.local_communications;

/**
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public interface Handler {
    public void handle(final byte[] message);
}
