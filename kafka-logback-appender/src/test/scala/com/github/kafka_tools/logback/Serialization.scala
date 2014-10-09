package com.github.kafka_tools.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{LoggingEventVO, ILoggingEvent, LoggingEvent}
import org.junit.{Assert, Test}

/**
 * Author: Evgeny Zhoga
 * Date: 10.09.14
 */
class Serialization {
  @Test
  def serializeDeserialize(): Unit = {
    val le: ILoggingEvent = new LoggingEvent {
      setLevel(Level.WARN)
      setLoggerName("logger name")
      setMessage("Halala")
    }

    val restored = deserialize.Proto.toLoggerEvent(serialize.LoggerEvent.toProto(le))

    Console.println()
    Assert.assertEquals(
      "Original message [%s] not equals to restored [%s]" format(le.toString, restored.toString),
      LoggingEventVO.build(le),
      LoggingEventVO.build(restored)
    )
  }
}
