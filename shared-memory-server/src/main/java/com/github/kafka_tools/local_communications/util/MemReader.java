package com.github.kafka_tools.local_communications.util;

import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;

/**
 * Author: Evgeny Zhoga
 * Date: 13.10.14
 */
public class MemReader {
    private MappedByteBuffer mem;
    public MemReader(MappedByteBuffer mem, int startPosition) {
        this.mem = mem;
        mem.position(startPosition);
    }
    public String readString() {
        return new String(readBytes(), Charset.forName("utf-8"));
    }
    public byte[] readBytes() {
        int length = mem.getInt();
        byte[] bytes = new byte[length];
        mem.get(bytes);
        return bytes;
    }
    public int readInt() {
        return mem.getInt();
    }
}
