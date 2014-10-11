package com.github.kafka_tools.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;
import com.github.kafka_tools.logback.deserialize.ProtoEvent;
import com.github.kafka_tools.logback.serialize.ILoggerEvent;
import org.junit.Assert;
import org.junit.Test;

/**
 * Author: Evgeny Zhoga
 * Date: 11.10.14
 */
public class SerializationTest {
    @Test
    public void serializeDeserialize() {
        LoggingEvent le = new LoggingEvent();
        le.setLevel(Level.WARN);
        le.setLoggerName("logger name");
        le.setMessage("Halala");


        ILoggingEvent restored = ProtoEvent.toLoggerEvent(ILoggerEvent.toProto(le));

        Assert.assertEquals(
                String.format("Original message [%s] not equals to restored [%s]", le.toString(), restored.toString()),
                LoggingEventVO.build(le),
                LoggingEventVO.build(restored)
        );
    }
}
