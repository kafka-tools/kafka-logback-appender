package com.github.kafka_tools.local_communications.client;

import com.github.kafka_tools.local_communications.*;
import com.github.kafka_tools.local_communications.server.HostServer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 14.10.14
 */
public class ClientFactory {
    public static <T extends ClientInfo> Client getClient(
            final String filesHome,
            final int requiredBufferSize,
            final T clientData
    ) throws IOException {
        try {
            File parent = new File(filesHome);
            final File f = new File(parent, HostServer.managingStream);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            FileChannel fc = raf.
                    getChannel();
            MappedByteBuffer mainChannel = fc.
                    map(FileChannel.MapMode.READ_WRITE, 0, 100);
            FileLock fileLock = fc.lock(0, 100, true);
            try {
                while (mainChannel.get(0) != Constants.HOST_WAITING) Thread.sleep(0);

                MemWriter memWriter = new MemWriter(mainChannel, 1);
                memWriter.write(requiredBufferSize);
                clientData.write(memWriter);
                mainChannel.put(0, Constants.HOST_MUST_READ);
                mainChannel.force();

                while (mainChannel.get(0) != Constants.CLIENT_MUST_READ) Thread.sleep(0);
                MemReader mr = new MemReader(mainChannel, 1);
                String streamName = mr.readString();

                mainChannel.put(0, Constants.CLIENT_STARTED);
                mainChannel.force();
                return new Client(new File(parent, streamName).getAbsolutePath(), requiredBufferSize);
            } finally {
                fileLock.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
