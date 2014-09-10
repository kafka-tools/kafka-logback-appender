package com.github.kafka_tools.kafka

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import ch.qos.logback.classic.spi.{ILoggingEvent, LoggingEventVO}
import kafka.utils.VerifiableProperties
import com.github.kafka_tools.logback.serialize

/**
 * Author: Evgeny Zhoga
 * Date: 18.07.14
 */
class ValueEncoder(props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Encoder[ILoggingEvent] {
  override def toBytes(t: ILoggingEvent): Array[Byte] = serializeAsProtobuf(t)

  def serializeAsProtobuf(t: ILoggingEvent): Array[Byte] = serialize.LoggerEvent.toProto(t).toByteArray

  def serializeAsJavaObject(t: ILoggingEvent): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    val objectOutput = new ObjectOutputStream(output)
    objectOutput.writeObject(LoggingEventVO.build(t))
    val result = output.toByteArray
    try {
      output.close()
      objectOutput.close()
    }
    result
  }
}
