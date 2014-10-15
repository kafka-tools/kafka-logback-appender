package com.github.kafka_tools.local_communications.server;

import com.github.kafka_tools.local_communications.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.kafka_tools.local_communications.Constants.*;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 13.10.14
 */
public class HostServer<T extends ClientInfo> implements Runnable {
    public static final String managingStream = "main";

    private final MappedByteBuffer mem;
    private final File home;
    private final HandlerFactory<T> handlerFactory;
    private final CommunicationInfo.Factory<T> cf;
    private AtomicBoolean running = new AtomicBoolean(false);

    public HostServer(
            final String filesHome,
            final long fileSize,
            final HandlerFactory<T> handlerFactory,
            final CommunicationInfo.Factory<T> cf
    ) throws IOException {
        this.cf = cf;
        this.home = new File(filesHome);
        this.handlerFactory = handlerFactory;
        if (!home.exists() && !home.mkdirs()) throw new RuntimeException(String.format("Cannot create dir [%s]", filesHome));
        final File f = new File(home, managingStream);

        if (f.exists() && !f.delete()) throw new RuntimeException(String.format("Cannot delete [%s]", f.getAbsolutePath()));
        final RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.setLength(fileSize);
        this.mem = raf.
                getChannel().
                map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
    }

    @Override
    public void run() {
        running.set(true);
        mem.put(0, HOST_WAITING);
        mem.force();
        while (running.get()) {
            try {
                while (inboxIsEmpty() && running.get()) Thread.sleep(0); // waiting for client request

                if (isClientData()) {
                    try {
                        MemReader mr = new MemReader(mem, 1);
                        int bufferSize = mr.readInt();
                        CommunicationInfo<T> communicationInfo = cf.build(generateStreamName(), bufferSize);
                        T clientInfo = communicationInfo.read(mr);

                        registerClient(communicationInfo, clientInfo);
                        MemWriter mw = new MemWriter(mem, 1);
                        mw.write(communicationInfo.getStreamName());
                        mem.put(0, CLIENT_MUST_READ);
                        mem.force();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (isClientConfirmed()) {
                    mem.put(0, HOST_WAITING);
                    mem.force();
                }
            } catch (InterruptedException e) {
                running.set(false);
            }
        }
    }

    private void registerClient(CommunicationInfo<T> communicationInfo, T clientInfo) throws IOException {
        new Thread(new Server(home, communicationInfo, handlerFactory.getHandler(clientInfo))).start();
    }

    private boolean inboxIsEmpty() {
        return mem.get(0) == HOST_WAITING || mem.get(0) == CLIENT_MUST_READ;
    }
    private boolean isClientData() {
        return mem.get(0) == HOST_MUST_READ;
    }
    private boolean isClientConfirmed() {
        return mem.get(0) == CLIENT_STARTED;
    }


    private static final String CONST = "0123456789abcdefghijklmnopqrstuvwxyz";
    private Random r = new Random();
    private String generateStreamName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < 32;i++) {
            sb.append(CONST.charAt(r.nextInt(CONST.length())));
        }
        return sb.toString();
    }
}
