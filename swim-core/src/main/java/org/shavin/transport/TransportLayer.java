package org.shavin.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

public interface TransportLayer {

    /**
     * Start listening on the specified port for incoming messages.
     * @param port the Port to be bind to
     * @param handler the callback to handle incoming raw messages
     */
    Future<Void> start(int port, MessageHandler handler);

    /**
     * Send a message to the specified address.
     * @param address the address to send the message to
     * @param data the message to send
     */
    void send(InetSocketAddress address, byte[] data);

    /**
     * Stop listening for incoming messages.
     */
    void stop();

}
