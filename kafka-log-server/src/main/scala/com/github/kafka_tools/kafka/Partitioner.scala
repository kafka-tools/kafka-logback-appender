package com.github.kafka_tools.kafka

import kafka.producer.{Partitioner => P}
import kafka.utils.VerifiableProperties

/**
 * Author: Evgeny Zhoga
 * Date: 18.07.14
 */
class Partitioner(props: VerifiableProperties = null) extends P() {
  def partition(key: Any, numPartitions: Int) = {
    math.abs(key.asInstanceOf[Key].host.hashCode) % numPartitions
  }
}
