package com.github.kafka_tools.local_communications.client;

import com.github.kafka_tools.local_communications.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private Pinger pinger;

    Client(
            final String sharedFilePath,
            final long sharedFileSize) throws IOException {
        this(sharedFilePath, sharedFileSize, false);
    }

    Client(
            final String sharedFilePath,
            final long sharedFileSize,
            final boolean initBuffer) throws IOException {
        if (initBuffer) {
            final File f = new File(sharedFilePath);
            if (f.exists() && f.length() != sharedFileSize) f.delete();
            if (!f.exists()) new RandomAccessFile(f, "rw").setLength(sharedFileSize);
        }
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
        pinger = new Pinger(Util.getWatchdogThreadName(sharedFilePath));
        new Thread(pinger).start();
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (pinger != null) pinger.stop();
    }

    private class Pinger implements Runnable {
        private AtomicBoolean running = new AtomicBoolean(false);
        private MappedByteBuffer wd;

        private Pinger(String path) throws IOException {
            final File f = new File(path);
            this.wd = new RandomAccessFile(f, "rw").
                    getChannel().
                    map(FileChannel.MapMode.READ_WRITE, 0, 1);
        }

        @Override
        public void run() {
            running.set(true);
            while(running.get()) {
                wd.put(0, (byte)1);
                wd.force();
            }
        }

        public void stop() {
            running.set(false);
        }
    }
}
