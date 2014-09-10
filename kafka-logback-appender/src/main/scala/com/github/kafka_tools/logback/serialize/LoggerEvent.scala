package com.github.kafka_tools.logback.serialize

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.{ILoggingEvent, IThrowableProxy, StackTraceElementProxy}
import org.slf4j.Marker

import scala.collection.JavaConversions._

/**
 * Author: Evgeny Zhoga
 * Date: 09.09.14
 */
object LoggerEvent {
  def toProto(t: ILoggingEvent): LogbackProto.ILoggingEvent = {
    val loggerContextVO = Option(t.getLoggerContextVO)

    val r = LogbackProto.ILoggingEvent.newBuilder()
    Option(t.getLoggerName).foreach(r.setLoggerName)
    Option(t.getLoggerContextVO).foreach(c => {
      val lc = LogbackProto.LoggerContext.newBuilder()
      Option(c.getName).foreach(lc.setName)
      Option(c.getPropertyMap).foreach(pm => {
        lc.addAllPropertyMap(pm.entrySet().map(entry2stringTuple2))
      })
      lc.setBirthTime(c.getBirthTime)
      r.setLoggerContext(lc)
    })
    Option(t.getThreadName).foreach(r.setThreadName)
    Option(t.getLevel).foreach(l => r.setLevel(level2level(l)))
    Option(t.getMessage).foreach(r.setMessage)
    Option(t.getArgumentArray).foreach(a => r.addAllArgumentArray(asJavaIterable(a.map(_.toString).toIterable)))
    Option(t.getMarker).foreach(m => r.setMarker(marker2marker(m)))
    Option(t.getMDCPropertyMap).foreach(m => r.addAllMdcPropertyMap(m.entrySet().map(entry2stringTuple2)))
    r.setTimestamp(t.getTimeStamp)
    Option(t.getThrowableProxy).foreach(t => r.setThrowable(throwable2thowable(t)))

    if (t.hasCallerData)
      r.addAllCallerDataArray(asJavaIterable(t.getCallerData.map(element2element).toIterable))

    r.build
  }

  private def level2level(l: Level): LogbackProto.Level = {
    if (l == Level.OFF) LogbackProto.Level.OFF
    else if (l == Level.ERROR) LogbackProto.Level.ERROR
    else if (l == Level.WARN) LogbackProto.Level.WARN
    else if (l == Level.INFO) LogbackProto.Level.INFO
    else if (l == Level.DEBUG) LogbackProto.Level.DEBUG
    else if (l == Level.TRACE) LogbackProto.Level.TRACE
    else if (l == Level.ALL) LogbackProto.Level.ALL
    else throw new RuntimeException("Unreachable exception")
  }

  private def marker2marker(m: Marker): LogbackProto.Marker = {
    val r = LogbackProto.Marker.newBuilder()
    Option(m.getName).foreach(r.setName)
    Option(m.iterator()).filterNot(_.isEmpty).foreach(ref =>
      r.addAllReferences(
        asJavaIterable(ref.
          map(_.asInstanceOf[Marker]).
          map(marker2marker).toIterable))
    )
    r.build()
  }

  private def entry2stringTuple2(entry: java.util.Map.Entry[String, String]): LogbackProto.StringTuple2 = {
    val e = LogbackProto.StringTuple2.newBuilder()
    e.setKey(entry.getKey)
    Option(entry.getValue).foreach(e.setValue)
    e.build()
  }

  private def throwable2thowable(t: IThrowableProxy): LogbackProto.Throwable = {
    val tr = LogbackProto.Throwable.newBuilder()
    Option(t.getClassName).foreach(tr.setClassName)
    Option(t.getMessage).foreach(tr.setMessage)
    tr.setCommonFramesCount(t.getCommonFrames)
    Option(t.getStackTraceElementProxyArray).foreach(a =>
      tr.addAllStackTraceElementProxyArray(asJavaIterable(a.map(element2element).toIterable))
    )
    Option(t.getCause).foreach(c => tr.setCause(throwable2thowable(c)))
    Option(t.getSuppressed).foreach(s =>
      tr.addAllSuppressed(asJavaIterable(s.map(throwable2thowable).toIterable))
    )
    tr.build()
  }

  private def element2element(ste: StackTraceElementProxy): LogbackProto.StackTraceElement = element2element(ste.getStackTraceElement)

  private def element2element(ste: StackTraceElement): LogbackProto.StackTraceElement = {
    val e = LogbackProto.StackTraceElement.newBuilder()
    Option(ste.getClassName).foreach(e.setDeclaringClass)
    Option(ste.getMethodName).foreach(e.setMethodName)
    Option(ste.getFileName).foreach(e.setFileName)
    e.setLineNumber(ste.getLineNumber)
    e.build()
  }
}
