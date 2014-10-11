package com.github.kafka_tools.logback.deserialize

import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.kafka_tools.logback.serialize.LogbackProto

import scala.collection.JavaConversions._

/**
 * Author: Evgeny Zhoga
 * Date: 10.09.14
 */
object Proto {
  /**
   * Deserialize ILoggingEvent from protobuf
   * @param p serialized log record
   * @return deserialized log record
   */
  def toLoggerEvent(p: LogbackProto.ILoggingEvent): ILoggingEvent = ProtoEvent.toLoggerEvent(p)
}
