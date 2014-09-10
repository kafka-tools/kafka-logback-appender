kafka-logback-appender
======================

Logback appender to Apache Kafka

Includes:
1. Logback appender
2. Protobuf model + serializer/deserializer
3. Test examples for logging application & consumer

To run test logging application:
com.github.kafka_tools.logback.TestLoggingApplication -Dhost.name=hostname.here -Dkafka.brokers=kafka1.server:9092,kafka1.server:9092

To run test consumer:
com.github.kafka_tools.logback.SimpleExample -Dkafka.brokers=kafka1.server,kafka2.server
