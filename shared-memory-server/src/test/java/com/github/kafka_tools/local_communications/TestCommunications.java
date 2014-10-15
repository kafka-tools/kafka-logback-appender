package com.github.kafka_tools.local_communications;

import com.github.kafka_tools.local_communications.client.Client;
import com.github.kafka_tools.local_communications.client.ClientFactory;
import com.github.kafka_tools.local_communications.server.HostServer;
import com.github.kafka_tools.local_communications.server.Server;
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

        HostServer<CI> hs = new HostServer<CI>(
                fileName,
                100,
                new HF(),
                new F()

        );
        serverThread =new Thread(hs, "host-server");
        serverThread.start();
/*
        Server server = new Server(fileName, fileSize, new Handler() {
            public void handle(byte[] message) {
                String m = new String(message, Charset.forName("utf-8"));
                System.out.println(String.format("[%s] [%s] message [%s] received", serverCounter, System.nanoTime(), m));
                Assert.assertTrue(messages.contains(m));
                serverCounter += 1;
            }
        });
*/

//        new Thread(server).start();
    }

    @Test
    public void testClientJava() throws IOException {
        Util.log("start test");
        Client c1 = ClientFactory.getClient(fileName, fileSize, new CI("topic1"));
        Util.log("got c1");
        Client c2 = ClientFactory.getClient(fileName, fileSize, new CI("topic1"));
        Util.log("got c2");

        Charset utf8 = Charset.forName("utf-8");
        c1.send("to topic 1".getBytes(utf8));
        Util.log("sent to c1");
        c2.send("to topic 2".getBytes(utf8));
        Util.log("sent to c2");
    }

    @After
    public void stop() {
        serverThread.interrupt();
    }

    private class CI implements ClientInfo {
        private String topic;

        private CI(String topic) {
            this.topic = topic;
        }

        @Override
        public void write(MemWriter writer) {
            writer.write(topic);
        }
    }
    class F extends CommunicationInfo.Factory<CI> {
        @Override
        public CommunicationInfo<CI> build(String streamName, int bufferSize) {
            return new CommI(streamName, bufferSize);
        }
    }
    class CommI extends CommunicationInfo<CI> {
        CommI(String streamName, int bufferSize) {
            super(streamName, bufferSize);
        }

        @Override
        public CI read(MemReader in) {
            return new CI(in.readString());
        }
    }
    class HF implements HandlerFactory<CI> {
        @Override
        public Handler getHandler(final CI ci) {
            return new Handler() {
                @Override
                public void handle(byte[] message) {
                    Util.log(ci.topic + ": " + new String(message, Charset.forName("utf-8")));
                }
            };
        }
    }
}
