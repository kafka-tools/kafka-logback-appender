package com.github.kafka_tools.logback

import java.util.Properties

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.github.kafka_tools.kafka.{Key, Partitioner, ValueEncoder, KeyEncoder}
import kafka.javaapi.producer.Producer
import kafka.producer.{KeyedMessage, ProducerConfig}

/**
 * Main class of appender
 */
class KafkaAppender extends AppenderBase[ILoggingEvent] {

  @reflect.BeanProperty var topic: String = _
  @reflect.BeanProperty var brokers: String = _
  private var producer: Producer[Key, ILoggingEvent] = _
  private val getHostname = Option(System.getProperty("host.name")) getOrElse(throw new RuntimeException("host.name should be set"))

  override def start() {
    super.start()
    val props = new Properties
    props.put("metadata.broker.list", brokers)
    props.put("serializer.class", classOf[ValueEncoder].getName)
    props.put("key.serializer.class", classOf[KeyEncoder].getName)
    props.put("partitioner.class", classOf[Partitioner].getName)
    props.put("request.required.acks", "1")

    val config = new ProducerConfig(props)
    producer = new Producer[Key, ILoggingEvent](config)
  }
  override def stop() {
    super.stop()
    producer.close
  }

  protected def append(event: ILoggingEvent) {
    producer.send(
      new KeyedMessage[Key, ILoggingEvent](
        this.topic,
        Key(getHostname),
        event)
    )
  }

}
