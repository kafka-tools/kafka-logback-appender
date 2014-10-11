package com.github.kafka_tools.logback.serialize;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import org.slf4j.Marker;

import java.util.*;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 09.10.14
 */
public class ILoggerEvent {
    public static LogbackProto.ILoggingEvent toProto(final ILoggingEvent t) {
        LogbackProto.ILoggingEvent.Builder r = LogbackProto.ILoggingEvent.newBuilder();

        if (t.getLoggerName() != null) r.setLoggerName(t.getLoggerName());
        if (t.getLoggerContextVO() != null) {
            LoggerContextVO c = t.getLoggerContextVO();
            LogbackProto.LoggerContext.Builder lc = LogbackProto.LoggerContext.newBuilder();
            if (c.getName() != null) lc.setName(c.getName());
            if (c.getPropertyMap() != null) {
                java.util.Map<java.lang.String, java.lang.String> pm = c.getPropertyMap();
                List<LogbackProto.StringTuple2> pmn = new LinkedList<LogbackProto.StringTuple2>();
                for (Map.Entry<String, String> entry : pm.entrySet()) {
                    pmn.add(entry2stringTuple2(entry));
                }
                lc.addAllPropertyMap(pmn);
            }
            lc.setBirthTime(c.getBirthTime());
            r.setLoggerContext(lc.build());
        }
        if (t.getThreadName() != null) r.setThreadName(t.getThreadName());
        if (t.getLevel() != null) r.setLevel(level2level(t.getLevel()));
        if (t.getMessage() != null) r.setMessage(t.getMessage());
        if (t.getArgumentArray() != null) {
            Object[] a = t.getArgumentArray();
            String[] s = new String[a.length];
            for (int i = 0;i < a.length;i++) {
                s[i] = a[i].toString();
            }
            r.addAllArgumentArray(Arrays.asList(s));
        }
        if (t.getMarker() != null) r.setMarker(marker2marker(t.getMarker()));
        if (t.getMDCPropertyMap() != null) {
            java.util.Map<java.lang.String, java.lang.String> pm = t.getMDCPropertyMap();
            List<LogbackProto.StringTuple2> pmn = new LinkedList<LogbackProto.StringTuple2>();
            for (Map.Entry<String, String> entry : pm.entrySet()) {
                pmn.add(entry2stringTuple2(entry));
            }
            r.addAllMdcPropertyMap(pmn);
        }
        r.setTimestamp(t.getTimeStamp());
        if (t.getThrowableProxy() != null) r.setThrowable(throwable2thowable(t.getThrowableProxy()));
        if (t.hasCallerData()) {
            List<LogbackProto.StackTraceElement> mr = new LinkedList<LogbackProto.StackTraceElement>();
            for (StackTraceElement e:  t.getCallerData()) {
                mr.add(element2element(e));
            }
            r.addAllCallerDataArray(mr);
        }

        return r.build();
    }

    private static LogbackProto.StringTuple2 entry2stringTuple2(java.util.Map.Entry<String, String> entry) {
        LogbackProto.StringTuple2.Builder e = LogbackProto.StringTuple2.newBuilder();
        e.setKey(entry.getKey());
        if (entry.getValue() != null) e.setValue(entry.getValue());
        return e.build();
    }
    private static LogbackProto.Level level2level(Level l) {
        if (l == Level.OFF) return LogbackProto.Level.OFF;
        else if (l == Level.ERROR) return LogbackProto.Level.ERROR;
        else if (l == Level.WARN) return LogbackProto.Level.WARN;
        else if (l == Level.INFO) return LogbackProto.Level.INFO;
        else if (l == Level.DEBUG) return LogbackProto.Level.DEBUG;
        else if (l == Level.TRACE) return LogbackProto.Level.TRACE;
        else if (l == Level.ALL) return LogbackProto.Level.ALL;
        else throw new RuntimeException("Unreachable exception");
    }
    private static LogbackProto.Marker marker2marker(Marker m) {
        LogbackProto.Marker.Builder r = LogbackProto.Marker.newBuilder();
        if (m.getName() != null) r.setName(m.getName());
        Iterator i = m.iterator();
        if (i.hasNext()) {
            List<LogbackProto.Marker> mr = new LinkedList<LogbackProto.Marker>();
            while (i.hasNext()) {
                mr.add(marker2marker((Marker) i.next()));
            }
            r.addAllReferences(mr);
        }
        return r.build();
    }

    private static LogbackProto.Throwable throwable2thowable(IThrowableProxy t) {
        LogbackProto.Throwable.Builder r = LogbackProto.Throwable.newBuilder();
        if (t.getClassName() != null) r.setClassName(t.getClassName());
        if (t.getMessage() != null) r.setMessage(t.getMessage());
        r.setCommonFramesCount(t.getCommonFrames());
        if (t.getStackTraceElementProxyArray() != null) {
            List<LogbackProto.StackTraceElement> mr = new LinkedList<LogbackProto.StackTraceElement>();
            for (StackTraceElementProxy e: t.getStackTraceElementProxyArray()) {
                mr.add(element2element(e));
            }

            r.addAllStackTraceElementProxyArray(mr);
        }
        if (t.getCause() != null) r.setCause(throwable2thowable(t.getCause()));
        if (t.getSuppressed() != null) {
            List<LogbackProto.Throwable> mr = new LinkedList<LogbackProto.Throwable>();
            for (IThrowableProxy p : t.getSuppressed()) {
                mr.add(throwable2thowable(p));
            }
            r.addAllSuppressed(mr);
        }
        return r.build();
    }

    private static LogbackProto.StackTraceElement element2element(StackTraceElementProxy t) {
        return element2element(t.getStackTraceElement());
    }
    private static LogbackProto.StackTraceElement element2element(StackTraceElement t) {
        LogbackProto.StackTraceElement.Builder r = LogbackProto.StackTraceElement.newBuilder();
        if (t.getClassName() != null) r.setDeclaringClass(t.getClassName());
        if (t.getMethodName() != null) r.setMethodName(t.getMethodName());
        if (t.getFileName() != null) r.setFileName(t.getFileName());
        r.setLineNumber(t.getLineNumber());
        return r.build();
    }

}
