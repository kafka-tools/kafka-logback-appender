package com.github.kafka_tools.local_communications;

import com.github.kafka_tools.local_communications.client.Client;
import com.github.kafka_tools.local_communications.client.ClientFactory;
import com.github.kafka_tools.local_communications.client.impl.TopicClient;
import com.github.kafka_tools.local_communications.client.impl.TopicClientFactory;
import com.github.kafka_tools.local_communications.server.HostServer;
import org.junit.After;
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
    private Thread serverThread;

    @Before
    public void startServerJava() throws IOException {
        new File(fileName).delete();

        HostServer<TopicClient> hs = new HostServer<TopicClient>(
                fileName,
                new HF(),
                new TopicClientFactory()
        );
        serverThread =new Thread(hs, "host-server");
        serverThread.start();
    }

    @Test
    public void testClientJava() throws IOException {
        Client c1 = ClientFactory.getClient(fileName, fileSize, new TopicClient("topic1"));
        Client c2 = ClientFactory.getClient(fileName, fileSize, new TopicClient("topic2"));

        Charset utf8 = Charset.forName("utf-8");
        c1.send("to topic 1".getBytes(utf8));
        c2.send("to topic 2".getBytes(utf8));
    }

    @After
    public void stop() {
        serverThread.interrupt();
    }

    class HF implements HandlerFactory<TopicClient> {
        @Override
        public Handler getHandler(final TopicClient ci) {
            return new Handler() {
                @Override
                public void handle(byte[] message) {
                    Util.log(ci.topic + ": " + new String(message, Charset.forName("utf-8")));
                }
            };
        }
    }
}
