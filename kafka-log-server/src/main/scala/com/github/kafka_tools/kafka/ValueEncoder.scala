package com.github.kafka_tools.kafka

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import ch.qos.logback.classic.spi.{ILoggingEvent, LoggingEventVO}
import com.github.kafka_tools.logback.serialize
import kafka.utils.VerifiableProperties

/**
 * Author: Evgeny Zhoga
 * Date: 18.07.14
 */
class ValueEncoder(props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Encoder[Array[Byte]] {
  override def toBytes(t: Array[Byte]): Array[Byte] = t
}
