package com.github.kafka_tools.local_communications;

import java.io.IOException;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 15.10.14
 */
public class Exceptions {
    public static void runtime(String message, Object...args) {
        throw new RuntimeException(String.format(message, args));
    }

    public static void io(String message, Object...args) throws IOException {
        throw new IOException(String.format(message, args));
    }
}
