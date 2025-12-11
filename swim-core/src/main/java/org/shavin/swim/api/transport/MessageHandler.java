package org.shavin.swim.api.transport;

import java.net.InetSocketAddress;

/**
 * Functional interface for handling messages received from a network transport layer.
 * Implementations of this interface define the behavior to process message data and
 * details about the sender of the message.
 *
 * The {@code MessageHandler} is particularly useful in scenarios where messages
 * are asynchronously transmitted over a network, and a specific processing
 * logic for the incoming messages is required.
 *
 * This interface is designed to be used in conjunction with network transport
 * mechanisms, such as the {@code TransportLayer}, where incoming messages are
 * delivered to the provided {@code MessageHandler} instance for processing.
 *
 * Implementations should ensure thread-safety if the handler is expected to
 * be invoked concurrently by multiple threads.
 */
@FunctionalInterface
public interface MessageHandler {

    /**
     * Handles the incoming message data and details about the sender of the message.
     *
     * @param data the byte array representing the message data received from the sender.
     * @param sender the {@code InetSocketAddress} representing the sender's address.
     */
    void handle(byte[] data, InetSocketAddress sender);

}
