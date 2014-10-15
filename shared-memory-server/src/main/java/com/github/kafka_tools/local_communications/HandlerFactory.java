package com.github.kafka_tools.local_communications;

/**
 * User: Evgeny Zhoga <ezhoga@yandex-team.ru>
 * Date: 13.10.14
 */
public interface HandlerFactory<T extends ClientInfo> {
    public Handler getHandler(final T ci);
}
