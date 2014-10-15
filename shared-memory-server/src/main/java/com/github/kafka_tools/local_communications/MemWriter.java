package com.github.kafka_tools.local_communications;

import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;

/**
 * Author: Evgeny Zhoga
 * Date: 14.10.14
 */
public class MemWriter {
    private MappedByteBuffer mem;

    public MemWriter(MappedByteBuffer mem, int startPosiotion) {
        this.mem = mem;
        mem.position(startPosiotion);
    }
    public void write(String s) {
        write(s.getBytes(Charset.forName("utf-8")));
    }
    public void write(byte[] bytes) {
        mem.putInt(bytes.length);
        mem.put(bytes);
    }
    public void write(int number){
        mem.putInt(number);
    }
}
