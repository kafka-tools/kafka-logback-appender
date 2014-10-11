package com.github.kafka_tools.logback.deserialize;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.*;
import com.github.kafka_tools.logback.serialize.LogbackProto;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 10.10.14
 */
public class ProtoEvent {
    private static IMarkerFactory markerFactory = new BasicMarkerFactory();

    public static ILoggingEvent toLoggerEvent(final LogbackProto.ILoggingEvent p) {
        LoggingEvent r = new LoggingEvent() {
            public IThrowableProxy getThrowableProxy() {
                if (p.hasThrowable()) return throwable2throwable(p.getThrowable());
                else return null;
            }
        };

        r.setLoggerName(p.getLoggerName());
        r.setMessage(p.getMessage());
        if (p.hasLoggerContext()) {
            LogbackProto.LoggerContext c = p.getLoggerContext();
            Map<String, String> m = new HashMap<String, String>();
            for (LogbackProto.StringTuple2 e: c.getPropertyMapList()) {
                m.put(e.getKey(), e.getValue());
            }
            r.setLoggerContextRemoteView(
                    new LoggerContextVO(
                    c.getName(),
                    notEmpty(m),
                    c.getBirthTime()))
            ;
        }
        r.setLevel(level2level(p.getLevel()));
        r.setTimeStamp(p.getTimestamp());
        if (!p.getCallerDataArrayList().isEmpty()) {
            StackTraceElement[] me = new StackTraceElement[p.getCallerDataArrayList().size()];
            int i = 0;
            for (LogbackProto.StackTraceElement c: p.getCallerDataArrayList()) {
                me[i++] = new StackTraceElement(
                        c.getDeclaringClass(),
                        c.getMethodName(),
                        c.getFileName(),
                        c.getLineNumber()
                );
            }
            r.setCallerData(me);
        }

        Map<String,String> mdc = new HashMap<String, String>();
        for (LogbackProto.StringTuple2 e : p.getMdcPropertyMapList()) {
            mdc.put(e.getKey(), e.getValue());
        }
        r.setMDCPropertyMap(notEmpty(mdc));
        r.setThreadName(p.getThreadName());
        r.setArgumentArray(p.getArgumentArrayList().toArray());
        if (p.hasMarker()) {
            r.setMarker(marker2marker(p.getMarker()));
        }

        return r;
    }
    private static Marker marker2marker(LogbackProto.Marker m) {
        Marker bm = markerFactory.getDetachedMarker(m.getName());
        for (LogbackProto.Marker r : m.getReferencesList()) {
            bm.add(marker2marker(r));
        }
        return bm;
    }

    private static Level level2level(LogbackProto.Level l) {
        if (l == LogbackProto.Level.OFF) return Level.OFF;
        else if (l == LogbackProto.Level.ERROR) return Level.ERROR;
        else if (l == LogbackProto.Level.WARN) return Level.WARN;
        else if (l == LogbackProto.Level.INFO) return Level.INFO;
        else if (l == LogbackProto.Level.DEBUG) return Level.DEBUG;
        else if (l == LogbackProto.Level.TRACE) return Level.TRACE;
        else if (l == LogbackProto.Level.ALL) return Level.ALL;
        else return null;
    }

    private static <K, V> java.util.Map<K, V> notEmpty(java.util.Map<K, V> map) {
        if (map.isEmpty()) return null;
        else return map;
    }

    private static <T> T[] notEmpty(T[] array) {
        if (array.length == 0) return null;
        else return array;
    }

    private static IThrowableProxy throwable2throwable(final LogbackProto.Throwable t) {
        return new IThrowableProxy() {
            public String getMessage() {
                if (t.hasMessage()) return t.getMessage();
                else return null;
            }

            public int getCommonFrames() {
                return t.getCommonFramesCount();
            }

            public IThrowableProxy[] getSuppressed() {
                IThrowableProxy[] p = new IThrowableProxy[t.getSuppressedList().size()];
                int i = 0;
                for (LogbackProto.Throwable e : t.getSuppressedList()) {
                    p[i++] = throwable2throwable(e);
                }
                return notEmpty(p);
            }

            public StackTraceElementProxy[] getStackTraceElementProxyArray() {
                StackTraceElementProxy[] r = new StackTraceElementProxy[t.getStackTraceElementProxyArrayList().size()];
                int i = 0;
                for (LogbackProto.StackTraceElement e : t.getStackTraceElementProxyArrayList()) {
                    r[i++] = new StackTraceElementProxy(
                            new StackTraceElement(
                                    e.getDeclaringClass(),
                                    e.getMethodName(),
                                    e.getFileName(),
                                    e.getLineNumber()));
                }
                return notEmpty(r);
            }
            public String getClassName() {
                if (t.hasClassName()) return t.getClassName(); else return null;
            }
            public IThrowableProxy getCause() {
                if (t.hasCause()) return throwable2throwable(t.getCause()); else return null;
            }
        };
    }
}
