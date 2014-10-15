package com.github.kafka_tools.local_communications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 14.10.14
 */
public class Util {
    public static MappedByteBuffer map(File file,int size) throws IOException {
        return new RandomAccessFile(file, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
    }
    public static MappedByteBuffer map(String filePath,int size) throws IOException {
        return map(new File(filePath), size);
    }
    public static void ensure(File f, int size, boolean force) throws IOException {
        if ((f.exists() && f.length() != size) || force ) {
            if (f.delete()) new RandomAccessFile(f, "rw").setLength(size);
            else throw new IOException("Cannot delete file");
        }
    }
    public static void ensure(File f, int size) throws IOException {
        ensure(f, size, false);
    }
    public static String getWatchdogThreadName(String baseName) {
        return baseName + "_watchdog";
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
