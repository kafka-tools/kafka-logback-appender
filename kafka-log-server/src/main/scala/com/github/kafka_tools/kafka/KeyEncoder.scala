package com.github.kafka_tools.kafka

import kafka.utils.VerifiableProperties

/**
 * Author: Evgeny Zhoga
 * Date: 18.07.14
 */
class KeyEncoder(props: VerifiableProperties = null) extends scala.AnyRef with kafka.serializer.Encoder[Key] {
  override def toBytes(t: Key): Array[Byte] = {
    t.host.getBytes
  }
}
