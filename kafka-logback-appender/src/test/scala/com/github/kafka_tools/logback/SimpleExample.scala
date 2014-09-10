package com.github.kafka_tools.logback

/**
 * Based on from https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
 * Date: 10.09.14
 */

import com.github.kafka_tools.logback.serialize.LogbackProto
import kafka.api._
import kafka.common.{ErrorMapping, TopicAndPartition}
import kafka.consumer.SimpleConsumer

class SimpleExample {
  import com.github.kafka_tools.logback.SimpleExample._
  
  private var mReplicaBrokers: List[String] = _

  def run(aMaxReadsT: Long,
          aTopic: String,
          aPartition: Int,
          aSeedBrokers: List[String],
          aPort: Int) {
    var aMaxReads = aMaxReadsT
    // find the meta data about the topic and partition we are interested in
    //
    val metadata = findLeader(aSeedBrokers, aPort, aTopic, aPartition)
    if (metadata == null) {
      Console.println("Can't find metadata for Topic and Partition. Exiting")
      return
    }
    if (metadata.leader.isEmpty) {
      System.out.println("Can't find Leader for Topic and Partition. Exiting")
      return
    }
    var leadBroker = metadata.leader.get.host
    val clientName = "Client_" + aTopic + "_" + aPartition

    var consumer: SimpleConsumer = null
    try {
      consumer = new SimpleConsumer(leadBroker, aPort, 100000, 64 * 1024, clientName)
      var readOffset = getLastOffset(consumer, aTopic, aPartition, kafka.api.OffsetRequest.EarliestTime, clientName)

      var numErrors = 0
      while (aMaxReads > 0) {
        def getResult(iterations: Int = 5): FetchResponse = {
          if (consumer == null) {
            consumer = new SimpleConsumer(leadBroker, aPort, 100000, 64 * 1024, clientName)
          }

          if (iterations == 0) {
            throw new RuntimeException()
          }
          val req = new FetchRequestBuilder().
            clientId(clientName).
            addFetch(aTopic, aPartition, readOffset, 100000). // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
            build()
          val fetchResponse = consumer.fetch(req)

          if (!fetchResponse.hasError) {
            fetchResponse
          } else {
            // Something went wrong!
            val code = fetchResponse.errorCode(aTopic, aPartition)
            Console.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code)

            if (code == ErrorMapping.OffsetOutOfRangeCode) {
              // We asked for an invalid offset. For simple case ask for the last element to reset
              readOffset = getLastOffset(consumer, aTopic, aPartition, kafka.api.OffsetRequest.LatestTime, clientName)
            } else {
              consumer.close()
              consumer = null
              leadBroker = findNewLeader(leadBroker, aTopic, aPartition, aPort)
            }
            getResult(iterations - 1)
          }
        }
        val fetchResponse = getResult()

        var numRead = 0l
        for (messageAndOffset <- fetchResponse.messageSet(aTopic, aPartition)) {
          val currentOffset = messageAndOffset.offset
          if (currentOffset < readOffset) {
            Console.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset)
          } else {
            readOffset = messageAndOffset.nextOffset
            val payload = messageAndOffset.message.payload

            val bytes = new Array[Byte](payload.limit())
            payload.get(bytes)

//            Console.println(String.valueOf(messageAndOffset.offset) + ": " + new String(bytes, "UTF-8"))
            Console.println(String.valueOf(messageAndOffset.offset) + ": " + deserialize.Proto.toLoggerEvent(LogbackProto.ILoggingEvent.parseFrom(bytes)).toString)

            numRead += 1
            aMaxReads -= 1
          }
        }

        if (numRead == 0) {
          Thread.sleep(1000)
        }
      }
    } finally {
      if (consumer != null) consumer.close()
    }
  }


  private def findNewLeader(a_oldLeader: String, a_topic: String, a_partition: Int, a_port: Int, iterations: Int = 3, goToSleep: Boolean = false): String = {
    if (iterations == 0) {
      Console.println("Unable to find new leader after Broker failure. Exiting")
      throw new Exception("Unable to find new leader after Broker failure. Exiting")
    }
    if (goToSleep) {
      try {
        Thread.sleep(1000)
      } catch {
        case e: InterruptedException =>
      }
    }

    val metadata = findLeader(mReplicaBrokers, a_port, a_topic, a_partition)
    if (metadata == null) {
      findNewLeader(a_oldLeader, a_topic, a_partition, a_port, iterations - 1, goToSleep = true)
    } else if (metadata.leader == null) {
      findNewLeader(a_oldLeader, a_topic, a_partition, a_port, iterations - 1, goToSleep = true)
    } else if (a_oldLeader.equalsIgnoreCase(metadata.leader.get.host) && iterations == 3) {
      // first time through if the leader hasn't changed give ZooKeeper a second to recover
      // second time, assume the broker did recover before failover, or it was a non-Broker issue
      //
      findNewLeader(a_oldLeader, a_topic, a_partition, a_port, iterations - 1, goToSleep = true)
    } else {
      metadata.leader.get.host
    }
  }

  private def findLeader(aSeedBrokers: List[String], aPort: Int, aTopic: String, aPartition: Int): PartitionMetadata = {
    var returnMetaData: PartitionMetadata = null
//    loop:
    for (seed <- aSeedBrokers) {
      var consumer: SimpleConsumer = null
      try {
        consumer = new SimpleConsumer(seed, aPort, 100000, 64 * 1024, "leaderLookup")
        val topics = Seq(aTopic)
        val req = new TopicMetadataRequest(topics, correlationID)
        val resp = consumer.send(req)

        def find(i: Iterator[TopicMetadata]):PartitionMetadata  =
          if (!i.hasNext) null
          else i.next().partitionsMetadata.find(_.partitionId == aPartition).getOrElse(find(i))

        returnMetaData = find(resp.topicsMetadata.iterator)
      } catch {
        case e: Exception =>
          Console.println("Error communicating with Broker [" + seed + "] to find Leader for [" + aTopic
            + ", " + aPartition + "] Reason: " + e)
      } finally {
        if (consumer != null) consumer.close()
      }
    }
    if (returnMetaData != null) {
      mReplicaBrokers = returnMetaData.replicas.map(_.host).toList
    }
    returnMetaData
  }
}

object SimpleExample {
  val correlationID = 10

  private def getMaxReads(args: Array[String], default: Long): Long = {
    if (args.length > 0) args(0).toLong
    else default
  }
  private def getTopic(args: Array[String], default: String): String = {
    if (args.length > 1) args(1)
    else default
  }
  private def getPartition(args: Array[String], default: Int): Int = {
    if (args.length > 2) args(2).toInt
    else default
  }
  private def getSeeds(args: Array[String], default: List[String]): List[String] = {
    if (args.length > 3) args(3).split(",").toList
    else default
  }
  private def getPort(args: Array[String], default: Int): Int = {
    if (args.length > 4) args(4).toInt
    else default
  }

  def main(args: Array[String]) {
    val example = new SimpleExample()
    val maxReads = getMaxReads(args, 250l)
    val topic = getTopic(args, "example-proto-log")
    val partition = getPartition(args, 0)
    val seeds = getSeeds(args, System.getProperty("kafka.brokers").split(",").toList)
    val port = getPort(args, 9092)

    try {
      example.run(maxReads, topic, partition, seeds, port)
    } catch {
      case e: Exception =>
        Console.println("Oops:" + e)
        e.printStackTrace();
    }
  }

  def getLastOffset(consumer: SimpleConsumer,
                    topic: String,
                    partition: Int,
                    whichTime: Long,
                    clientName: String): Long = {
    val topicAndPartition = new TopicAndPartition(topic, partition)
    val requestInfo = Map(topicAndPartition -> new PartitionOffsetRequestInfo(whichTime, 1))

    val request = new kafka.api.OffsetRequest(
      requestInfo = requestInfo, correlationId = correlationID)
    val response = consumer.getOffsetsBefore(request)

    if (response.hasError) {
      Console.println("Error fetching data Offset Data the Broker. Reason: " + response.describe(details = true))
      0l
    } else {
      response.offsetsGroupedByTopic.
        get(topic).
        map(m => m.get(new TopicAndPartition(topic, partition)).map(por => por.offsets.headOption.getOrElse(0l)).getOrElse(0l)).getOrElse(0l)
//      val offsets = response.offsets(topic, partition)
      //offsets(0)
    }
  }

}
