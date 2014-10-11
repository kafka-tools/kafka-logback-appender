package com.github.kafka_tools.logback.serialize

import ch.qos.logback.classic.spi.ILoggingEvent

/**
 * Author: Evgeny Zhoga
 * Date: 09.09.14
 */
object LoggerEvent {
  def toProto(t: ILoggingEvent): LogbackProto.ILoggingEvent = ILoggerEvent.toProto(t)
}
