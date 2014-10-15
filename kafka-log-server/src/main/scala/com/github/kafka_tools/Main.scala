package com.github.kafka_tools

import java.util.Properties

import _root_.kafka.javaapi.producer.Producer
import _root_.kafka.producer.{KeyedMessage, ProducerConfig}
import ch.qos.logback.classic.spi.ILoggingEvent
import com.github.kafka_tools.kafka.{Key, Partitioner, KeyEncoder, ValueEncoder}
import com.github.kafka_tools.local_communications.client.impl.TopicClient
import com.github.kafka_tools.local_communications.{HandlerFactory, Handler}
import com.github.kafka_tools.local_communications.server.EntityServer

/**
 * Author: Evgeny Zhoga
 * Date: 11.10.14
 */
object Main extends App {
  val fileName = Option(System.getProperty("kafka-tools.server.file-name")).getOrElse("/tmp/kafka.bridge")
  val brokers = Option(System.getProperty("kafka-tools.kafka.brokers")).getOrElse(throw new RuntimeException("Property kafka-tools.kafka.brokers should be set!"))

}

object KafkaHandlerFactory extends HandlerFactory[TopicClient] {
  override def getHandler(ci: TopicClient): Handler = new Handler {
    val props = new Properties
    props.put("metadata.broker.list", Main.brokers)
    props.put("serializer.class", classOf[ValueEncoder].getName)
    props.put("key.serializer.class", classOf[KeyEncoder].getName)
    props.put("partitioner.class", classOf[Partitioner].getName)
    props.put("request.required.acks", "1")

    val config = new ProducerConfig(props)
    val producer = new Producer[Key, Array[Byte]](config)
    val getHostname = Option(System.getProperty("host.name")) getOrElse(throw new RuntimeException("host.name should be set"))

    override def handle(message: Array[Byte]): Unit = {
      producer.send(
        new KeyedMessage[Key, Array[Byte]](
          ci.topic,
          Key(getHostname),
          message)
      )
    }
  }
}

