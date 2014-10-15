package com.github.kafka_tools.local_communications;

/**
 * Author: Evgeny Zhoga
 * Date: 09.10.14
 */
public class Constants {
    public static final byte notread = 3;
    public static final byte read = 4;
    public static final byte reset = 5;

    /**
     * host server is waiting for data
     */
    public static final byte HOST_WAITING = 11;
    /**
     * Client send data for host server
     */
    public static final byte HOST_MUST_READ = 12;
    /**
     * Host sends data data to client
     */
    public static final byte CLIENT_MUST_READ = 13;
    /**
     * Client is ready
     */
    public static final byte CLIENT_STARTED = 14;

    public static final int intSize = Integer.SIZE / 8;

}
