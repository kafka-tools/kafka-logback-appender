package com.github.kafka_tools.local_communications;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import static com.github.kafka_tools.local_communications.Constants.*;

/**
 * Idea is taken from http://stackoverflow.com/questions/2635272/fastest-low-latency-method-for-inter-process-communication-between-java-and-c
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public class Server {
    private final MappedByteBuffer mem;
    private int offset;
    private final Handler handler;

    public Server(
            final String sharedFilePath,
            final long sharedFileSize,
            final Handler handler
    ) throws IOException {
        final File f = new File(sharedFilePath);
        if (f.exists() && f.length() != sharedFileSize) f.delete();
        if (!f.exists()) new RandomAccessFile(f, "rw").setLength(sharedFileSize);

        this.handler = handler;
        this.mem = new RandomAccessFile(sharedFilePath, "rw").
                getChannel().
                map(FileChannel.MapMode.READ_WRITE, 0, sharedFileSize);
    }
    public void run() throws InterruptedException {
        while (true) {
            while (inboxIsEmpty()) Thread.sleep(0); // waiting for client request
            if (isReset()) offset = 0; // client was restarted

            int newOffset = (mem.get(offset) == reset) ? 0 : readNext();
            mem.put(offset, read);
            offset = newOffset;
        }
    }

    private boolean inboxIsEmpty() {
        return mem.get(offset) != notread && mem.get(offset) != reset && mem.get(0) != notread;
    }
    private boolean isReset() {
        return mem.get(offset) != notread && mem.get(offset) != reset && mem.get(0) == notread;
    }

    private int readNext() {
        int nOffset = offset + 1;
        int length = mem.getInt(nOffset);
        byte[] bytes = new byte[length];
        mem.position(nOffset + intSize);
        mem.get(bytes);
        handler.handle(bytes);
        return nOffset + intSize + length;
    }

}
