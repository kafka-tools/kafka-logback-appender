package com.github.kafka_tools

import com.github.kafka_tools.local_communications.Handler
import com.github.kafka_tools.local_communications.server.EntityServer

/**
 * Author: Evgeny Zhoga
 * Date: 11.10.14
 */
class Main extends App {
  val fileName = Option(System.getProperty("kafka-tools.server.file-name")).getOrElse(throw new RuntimeException("Use -Dkafka-tools.server.file-name=<buffer file name> to set file"))
  val fileSize = Option(System.getProperty("kafka-tools.server.file-size")).map(_.toLong).getOrElse(8l * 1024 * 1024)

}
