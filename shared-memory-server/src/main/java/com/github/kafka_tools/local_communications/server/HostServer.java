package com.github.kafka_tools.local_communications.server;

import com.github.kafka_tools.local_communications.*;
import com.github.kafka_tools.local_communications.util.Exceptions;
import com.github.kafka_tools.local_communications.util.MemReader;
import com.github.kafka_tools.local_communications.util.MemWriter;
import com.github.kafka_tools.local_communications.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.kafka_tools.local_communications.Constants.*;

/**
 * Main server element. Register main exchange stream to add new clients.
 *
 * Author: Evgeny Zhoga
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
            final HandlerFactory<T> handlerFactory,
            final CommunicationInfo.Factory<T> cf) throws IOException {
        this(filesHome, 100, handlerFactory, cf);
    }
    /**
     * Creates new host server on top of filesHome directory. filesHome folder will be re-created if exists
     *
     * @param filesHome folder to keep main stream and all client streams
     * @param fileSize default size for main stream
     * @param handlerFactory factory to generate handler depending on client
     * @param cf factory to generate CommunicationInfo entities, who knows how to read ClientInfo
     * @throws IOException
     */
    public HostServer(
            final String filesHome,
            final int fileSize,
            final HandlerFactory<T> handlerFactory,
            final CommunicationInfo.Factory<T> cf
    ) throws IOException {
        this.cf = cf;
        this.home = new File(filesHome);
        this.handlerFactory = handlerFactory;
        if (home.exists()) {
            for (File childStream: home.listFiles()) {
                childStream.delete();
            }
            home.delete();
        }
        if (!home.exists() && !home.mkdirs()) Exceptions.io("Cannot create dir [%s]", filesHome);
        final File f = new File(home, managingStream);

        Util.ensure(f, fileSize, true);
        this.mem = Util.map(f, fileSize);
    }

    @Override
    public void run() {
        running.set(true);
        Util.set0byte(mem, HOST_WAITING);
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
                        Util.set0byte(mem, CLIENT_MUST_READ);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (isClientConfirmed()) {
                    Util.set0byte(mem, HOST_WAITING);
                }
            } catch (InterruptedException e) {
                running.set(false);
                // removes stream files
                for (File childStream: home.listFiles()) {
                    childStream.delete();
                }
            }
        }
    }

    private void registerClient(CommunicationInfo<T> communicationInfo, T clientInfo) throws IOException {
        new Thread(new EntityServer(home, communicationInfo, handlerFactory.getHandler(clientInfo))).start();
    }

    private boolean inboxIsEmpty() {
        return Util.check0byte(mem, HOST_WAITING, CLIENT_MUST_READ);
    }
    private boolean isClientData() {
        return Util.check0byte(mem, HOST_MUST_READ);
    }
    private boolean isClientConfirmed() {
        return Util.check0byte(mem, CLIENT_STARTED);
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
