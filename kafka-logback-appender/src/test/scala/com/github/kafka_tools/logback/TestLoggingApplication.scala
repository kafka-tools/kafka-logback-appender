package com.github.kafka_tools.logback

import org.slf4j.LoggerFactory

/**
 * Author: Evgeny Zhoga
 * Date: 10.09.14
 */
object TestLoggingApplication {
  val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    for (i <- Range(1, 10)) {
      log.info("This is info log message [%s]" format i)
      Thread.sleep(1000)
      log.debug("This is debug log message")
      Thread.sleep(1000)
      log.error("This is error log message", new RuntimeException(new Error()))
      Thread.sleep(1000)
    }
  }
}
