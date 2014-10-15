package com.github.kafka_tools.local_communications.server;

import com.github.kafka_tools.local_communications.CommunicationInfo;
import com.github.kafka_tools.local_communications.Handler;
import com.github.kafka_tools.local_communications.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.kafka_tools.local_communications.Constants.*;

/**
 * Idea is taken from http://stackoverflow.com/questions/2635272/fastest-low-latency-method-for-inter-process-communication-between-java-and-c
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public class Server implements Runnable {
    private final MappedByteBuffer mem;
    private int offset;
    private final Handler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Watchdog w;

    public Server(
            final File home,
            final CommunicationInfo ci,
            final Handler handler
    ) throws IOException {
        this.handler = handler;
        final File f = new File(home, ci.getStreamName());
        Util.ensure(f, ci.getBufferSize());
        this.mem = Util.map(f, ci.getBufferSize());
        w = new Watchdog(new File(home, ci.getWatchdogStreamName()));
    }

    public void run() {
        running.set(true);
        new Thread(w).start();

        while (running.get()) {
            try {
                while (inboxIsEmpty() && running.get()) Thread.sleep(0); // waiting for client request
                if (running.get()) {
                    if (isReset()) offset = 0; // client was restarted

                    int newOffset = (mem.get(offset) == reset) ? 0 : readNext();
                    mem.put(offset, read);
                    offset = newOffset;
                }
            } catch (InterruptedException e) {
                stop();
            }
        }
    }
    public boolean isRunning() {
        return running.get();
    }

    public void stop() {
        running.set(false);
        w.stop.set(true);
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
    private class Watchdog implements Runnable {
        private final MappedByteBuffer mem;
        private int counter = 0;
        private AtomicBoolean stop = new AtomicBoolean(false);

        public Watchdog(
                final File sharedFile
        ) throws IOException {
            Util.ensure(sharedFile, 1);
            this.mem = Util.map(sharedFile, 1);
        }

        @Override
        public void run() {
            while (!stop.get() && counter < 5) {
                if (mem.get(0) == 1) {
                    counter = 0;
                    mem.put(0, (byte)0);
                    mem.force();
                } else {
                    counter ++;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    counter = 5;
                }
            }
            // 5 seconds no updates from client, so we stop server
            stop();
        }
    }
}
