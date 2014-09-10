kafka-logback-appender
======================

Logback appender to Apache Kafka

Includes:
<ol>
<li>Logback appender</li>
<li>Protobuf model + serializer/deserializer</li>
<li>Test examples for logging application & consumer</li>
</ol>

To run test logging application:<br>
<code>com.github.kafka_tools.logback.TestLoggingApplication -Dhost.name=hostname.here -Dkafka.brokers=kafka1.server:9092,kafka1.server:9092</code>

To run test consumer:<br>
<code>com.github.kafka_tools.logback.SimpleExample -Dkafka.brokers=kafka1.server,kafka2.server</code>
