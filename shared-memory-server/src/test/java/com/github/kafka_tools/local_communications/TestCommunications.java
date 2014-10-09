package com.github.kafka_tools.local_communications;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public class TestCommunications {
    private String fileName = "/tmp/bridge";
    private int fileSize = 1024 * 10;
    private List<String> messages = Arrays.asList("message one", "message 12345 123465", "mmm");
    private Thread serverThread;

    private int serverCounter;
    private int clientCounter;

    @Before
    public void startServerJava() throws IOException {
        serverCounter = 0;
        new File(fileName).delete();

        serverThread = new Thread(new Runnable() {
            Server server = new Server(fileName, fileSize, new Handler() {
                public void handle(byte[] message) {
                    String m = new String(message, Charset.forName("utf-8"));
                    System.out.println(String.format("[%s] [%s] message [%s] received", serverCounter, System.nanoTime(), m));
                    Assert.assertTrue(messages.contains(m));
                    serverCounter += 1;
                }
            });

            public void run() {
                try {
                    server.run();
                } catch (InterruptedException e) {
                    System.out.println("server was stopped");
                }

            }
        });
        serverThread.start();
    }

    @Test
    public void testClientJava() throws IOException {
        Client client = new Client(fileName, fileSize);
        clientCounter = 0;
        for (int i = 0;i < 1000;i++) {
            for (String m : messages) {
                System.out.println(String.format("[%s] [%s] message [%s] will be sent", clientCounter, System.nanoTime(), m));
                client.send(m.getBytes(Charset.forName("utf-8")));
                clientCounter += 1;
            }
        }
    }

    @After
    public void stop() {
        serverThread.interrupt();
    }
}
