package com.github.kafka_tools.logback.deserialize

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi._
import com.github.kafka_tools.logback.serialize.LogbackProto
import org.slf4j.Marker
import org.slf4j.helpers.BasicMarkerFactory

import scala.collection.JavaConversions._

/**
 * Author: Evgeny Zhoga
 * Date: 10.09.14
 */
object Proto {
  private val markerFactory = new BasicMarkerFactory

  /**
   * Deserialize ILoggingEvent from protobuf
   * @param p serialized log record
   * @return deserialized log record
   */
  def toLoggerEvent(p: LogbackProto.ILoggingEvent): ILoggingEvent = {
    new LoggingEvent() {
      setLoggerName(p.getLoggerName)
      setMessage(p.getMessage)
      if (p.hasLoggerContext) setLoggerContextRemoteView(
        new LoggerContextVO(
          p.getLoggerContext.getName,
          notEmpty(mapAsJavaMap(p.getLoggerContext.getPropertyMapList.map(t => (t.getKey, t.getValue)).toMap)),
          p.getLoggerContext.getBirthTime)
      )
      setLevel(level2level(p.getLevel))
      setTimeStamp(p.getTimestamp)
      if (Option(p.getCallerDataArrayList).exists(_.isEmpty)) {
        setCallerData(notEmpty(p.getCallerDataArrayList.map(c => new StackTraceElement(
          c.getDeclaringClass,
          c.getMethodName,
          c.getFileName,
          c.getLineNumber
        )).toArray))
      }
      setMDCPropertyMap(notEmpty(mapAsJavaMap(p.getMdcPropertyMapList.map(t => (t.getKey, t.getValue)).toMap)))
      setThreadName(p.getThreadName)
      setArgumentArray(notEmpty(p.getArgumentArrayList.toArray))
      if (p.hasMarker) setMarker({
        def marker2marker(m: LogbackProto.Marker): Marker = {
          val bm = markerFactory.getDetachedMarker(m.getName)
          m.getReferencesList.foreach(r => bm.add(marker2marker(r)))
          bm
        }
        marker2marker(p.getMarker)
      })

      override val getThrowableProxy: IThrowableProxy = if (p.hasThrowable) throwable2throwable(p.getThrowable) else null
    }
  }

  private def notEmpty[K, V](map: java.util.Map[K, V]): java.util.Map[K, V] = if (map.isEmpty) null else map

  private def notEmpty[T](array: Array[T]): Array[T] = if (array.length == 0) null else array

  private def level2level(l: LogbackProto.Level): Level = {
    if (l == LogbackProto.Level.OFF) Level.OFF
    else if (l == LogbackProto.Level.ERROR) Level.ERROR
    else if (l == LogbackProto.Level.WARN) Level.WARN
    else if (l == LogbackProto.Level.INFO) Level.INFO
    else if (l == LogbackProto.Level.DEBUG) Level.DEBUG
    else if (l == LogbackProto.Level.TRACE) Level.TRACE
    else if (l == LogbackProto.Level.ALL) Level.ALL
    else null
  }

  private def throwable2throwable(t: LogbackProto.Throwable): IThrowableProxy = {
    new IThrowableProxy {
      override def getMessage: String = if (t.hasMessage) t.getMessage else null

      override def getCommonFrames: Int = t.getCommonFramesCount

      override def getSuppressed: Array[IThrowableProxy] = notEmpty(t.getSuppressedList.map(throwable2throwable).toArray)

      override def getStackTraceElementProxyArray: Array[StackTraceElementProxy] =
        notEmpty(t.getStackTraceElementProxyArrayList.map(e => {
          new StackTraceElementProxy(new StackTraceElement(
            e.getDeclaringClass,
            e.getMethodName,
            e.getFileName,
            e.getLineNumber
          ))
        }).toArray)

      override def getClassName: String = if (t.hasClassName) t.getClassName else null

      override def getCause: IThrowableProxy = if (t.hasCause) throwable2throwable(t.getCause) else null
    }
  }

}
