package com.github.kafka_tools.local_communications.client;

import com.github.kafka_tools.local_communications.*;
import com.github.kafka_tools.local_communications.server.HostServer;
import com.github.kafka_tools.local_communications.util.MemReader;
import com.github.kafka_tools.local_communications.util.MemWriter;
import com.github.kafka_tools.local_communications.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Author: Evgeny Zhoga
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
                while (!Util.check0byte(mainChannel, Constants.HOST_WAITING)) Thread.sleep(0);

                MemWriter memWriter = new MemWriter(mainChannel, 1);
                memWriter.write(requiredBufferSize);
                clientData.write(memWriter);
                Util.set0byte(mainChannel, Constants.HOST_MUST_READ);

                while (!Util.check0byte(mainChannel, Constants.CLIENT_MUST_READ)) Thread.sleep(0);
                MemReader mr = new MemReader(mainChannel, 1);
                String streamName = mr.readString();

                Util.set0byte(mainChannel, Constants.CLIENT_STARTED);
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
