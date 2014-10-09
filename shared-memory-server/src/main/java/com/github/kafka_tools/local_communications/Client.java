package com.github.kafka_tools.local_communications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static com.github.kafka_tools.local_communications.Constants.*;

/**
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public class Client {
    private MappedByteBuffer mem;
    private final String sharedFilePath;
    private final long sharedFileSize;
    private int offset;
    private boolean bufferExists = false;

    public Client(
            final String sharedFilePath,
            final long sharedFileSize) throws IOException {
        this.sharedFileSize = sharedFileSize;
        this.sharedFilePath = sharedFilePath;
        if (check()) init();
    }

    private boolean check() {
        bufferExists = bufferExists || new File(sharedFilePath).exists();
        return bufferExists;
    }

    private void init() throws IOException {
        this.mem = new RandomAccessFile(sharedFilePath, "rw").
                getChannel().
                map(FileChannel.MapMode.READ_WRITE, 0, sharedFileSize);
    }

    public void send(final byte[] message) throws IOException {
        if (check() && this.mem == null) init();
        if (bufferExists) { // we do not check here file existence
            if (offset + 1 + intSize + message.length >= sharedFileSize) {
                mem.put(offset, reset);
                offset = 0;
            }
            if (offset + 1 + intSize + message.length >= sharedFileSize) {
                // message too long
            } else {
                mem.putInt(offset + 1, message.length);
                mem.position(offset + 1 + intSize);
                mem.put(message);
                mem.put(offset, notread);
                offset += 1 + intSize + message.length;
                mem.force();
            }
        } else {
            // nothing to do
        }
    }
}
