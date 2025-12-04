package org.shavin.api.transport;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface MessageHandler {

    void handle(byte[] data, InetSocketAddress sender);

}
