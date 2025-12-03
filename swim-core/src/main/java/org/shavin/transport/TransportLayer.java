package org.shavin.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * Represents a transport layer for network communication. This interface provides methods to
 * start listening for incoming messages on a specified port, send messages to a specific address,
 * and stop the transport layer.
 *
 * @author shavin
 */
public interface TransportLayer {

    /**
     * Starts the transport layer and begins listening for incoming messages on the specified port.
     * Messages received will be handled using the provided {@code MessageHandler}.
     *
     * @param port the port on which the transport layer will listen for incoming messages
     * @param handler the handler to process incoming messages, which receives the message data
     *                and the sender's address
     * @return a {@code Future} instance that completes once the transport layer has successfully started
     *         or fails if an error occurs during startup
     */
    Future<Void> start(int port, MessageHandler handler);

    /**
     * Sends a message to the specified network address.
     *
     * @param address the destination address to which the message should be sent.
     * @param data the byte array representing the message data to be sent.
     */
    void send(InetSocketAddress address, byte[] data);

    /**
     * Stops the transport layer from listening for incoming messages and releases any resources
     * associated with the transport layer. This method should be called to gracefully shut down
     * the transport layer and ensure all resources are cleaned up. Once stopped, the transport
     * layer cannot be reused without being restarted.
     */
    void stop();

}
