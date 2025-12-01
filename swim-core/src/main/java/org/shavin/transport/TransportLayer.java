package org.shavin.transport;

import java.net.InetSocketAddress;

public interface TransportLayer {

    /**
     * Start listening on the specified port for incoming messages.
     * @param port the Port to be bind to
     * @param handler the callback to handle incoming raw messages
     */
    void start(int port, MessageHandler handler);

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
